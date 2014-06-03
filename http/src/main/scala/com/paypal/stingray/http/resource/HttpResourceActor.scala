package com.paypal.stingray.http.resource

import akka.actor._
import scala.util.{Success, Try}
import spray.http._
import spray.http.StatusCodes._
import spray.http.Uri.Path
import spray.http.HttpHeaders.{RawHeader, `WWW-Authenticate`, Location}
import spray.http.HttpEntity.{Empty, NonEmpty}
import spray.http.{HttpRequest, HttpResponse}
import spray.routing.RequestContext
import com.paypal.stingray.akka.actor._
import com.paypal.stingray.common.constants.ValueConstants._
import com.paypal.stingray.http.util.HttpUtil
import scala.concurrent.duration._
import scala.reflect.ClassTag
import com.paypal.stingray.common.option._
import akka.actor.SupervisorStrategy.Escalate
import com.paypal.stingray.http.resource.HttpResourceActor.ResourceContext

/**
 * the actor to manage the execution of an [[AbstractResourceActor]]. create one of these per request
 * @param resourceCreator the instantiated actor object which will process the business logic for this request
 * @param reqContext the spray [[RequestContext]] for this request
 * @param reqParser the function to parse the request into a valid scala type
 * @param mbReturnActor the actor to send the successful [[HttpResponse]] or the failed [[Throwable]]. optional - pass None to not do this
 * @param recvTimeout the longest time this actor will wait for any step (except the request processsing) to complete.
 *                    if this actor doesn't execute a step in time, it immediately fails and sends an [[HttpResponse]] indicating the error to the
 *                    context and return actor.
 * @param processRecvTimeout the longest time this actor will wait for `reqProcessor` to complete
 * @tparam ParsedRequest type to parse the request to
 */
class HttpResourceActor[ParsedRequest](resourceCreator: ResourceContext => AbstractResourceActor,
                                       reqContext: RequestContext,
                                       reqParser: HttpResourceActor.RequestParser[ParsedRequest],
                                       mbReturnActor: Option[ActorRef],
                                       recvTimeout: Duration = HttpResourceActor.defaultRecvTimeout,
                                       processRecvTimeout: Duration = HttpResourceActor.defaultProcessRecvTimeout) extends ServiceActor {

  import HttpResourceActor._


  /*
   * Internal
   */
  case class RequestIsParsed(p: ParsedRequest)
  case object ContentTypeIsSupported
  case object ResponseContentTypeIsAcceptable



  private var pendingStep: Class[_] = HttpResourceActor.Start.getClass

  private var resourceActor: ActorRef = _

  private var mbSupportedFormats: Option[SupportedFormats] = None
  //This should never throw in normal operation but does need to be brought into state somehow
  //This is set by the first step in the pipeline, in the SupportedFormats step, and is used after that
  private lazy val unsafeSupportedFormats: SupportedFormats = mbSupportedFormats
    .orThrow(new IllegalStateException("VERY ILLEGAL STATE: Supported formats accessed before being set"))
  private def setNextStep[T](implicit classTag: ClassTag[T]): Unit = {
    pendingStep = classTag.runtimeClass
  }

  private val request = reqContext.request

  context.setReceiveTimeout(recvTimeout)


  override def preStart(): Unit = {
    resourceActor = context.actorOf(Props(resourceCreator(ResourceContext(self)))
      .withDispatcher(dispatcherName)
      .withMailbox("single-consumer-mailbox"))
    super.preStart()
  }

  //crash on unhandled exceptions
  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }

  override def receive: Actor.Receive = { // scalastyle:ignore cyclomatic.complexity scalastyle:ignore method.length

    //begin processing the request
    case Start =>
      setNextStep[SupportedFormats]
      //start the child actor as resourceActor
      resourceActor ! CheckSupportedFormats

    case formats: SupportedFormats =>
      setNextStep[ContentTypeIsSupported.type]
      mbSupportedFormats = formats.opt
      self ! ensureContentTypeSupported().map { _ =>
        ContentTypeIsSupported
      }.orFailure

    //the content type is supported, now check if the response content type is acceptable
    case ContentTypeIsSupported =>
      setNextStep[ResponseContentTypeIsAcceptable.type]
      self ! ensureResponseContentTypeAcceptable().map { _ =>
        ResponseContentTypeIsAcceptable
      }.orFailure

    //the response content type is acceptable, now check if the request is authorized
    case ResponseContentTypeIsAcceptable =>
      setNextStep[RequestIsParsed]
      self ! reqParser.apply(request).map { p =>
        RequestIsParsed(p)
      }.orFailure

    //the request has been parsed, now check if the content type is supported
    case RequestIsParsed(p) =>
      setNextStep[RequestIsProcessed]
      //account for extremely long processing times
      context.setReceiveTimeout(processRecvTimeout)
      resourceActor ! ProcessRequest(p)

    //the request has been processed, now construct the response, send it to the spray context, send it to the returnActor, and stop
    case RequestIsProcessed(resp, mbLocation) =>
      setNextStep[HttpResponse]
      context.setReceiveTimeout(recvTimeout)
      val responseWithLocation = addHeaderOnCode(resp, Created) {
        // if an `X-Forwarded-Proto` header exists, read the scheme from that; else, preserve what was given to us
        val newScheme = request.headers.find(_.name == "X-Forwarded-Proto") match {
          case Some(hdr) => hdr.value
          case None => request.uri.scheme
        }

        // if we created something, `location` will have more information to append to the response path
        val finalLocation = mbLocation match {
          case Some(loc) => s"/$loc"
          case None => ""
        }
        val newPath = Path(request.uri.path.toString + finalLocation)

        // copy the request uri, replacing scheme and path as needed, and return a `Location` header with the new uri
        val newUri = request.uri.copy(scheme = newScheme, path = newPath)
        Location(newUri)
      }
      val headers = addLanguageHeader(unsafeSupportedFormats.responseLanguage, responseWithLocation.headers)
      // Just force the request to the right content type

      val finalResponse: HttpResponse = responseWithLocation.withHeadersAndEntity(headers, responseWithLocation.entity.flatMap {
        entity: NonEmpty =>
          HttpEntity(unsafeSupportedFormats.responseContentType, entity.data)
      })

      self ! finalResponse

    //we got a response to return (either through successful processing or an error handling),
    //so return it to the spray context and return actor and then stop
    case r: HttpResponse =>
      reqContext.complete(r)
      mbReturnActor.foreach { returnActor =>
        returnActor ! r
      }
      context.stop(self)

    //there was an error somewhere along the way, so translate it to an HttpResponse (using handleError),
    //send the exception to returnActor and stop
    case s @ Status.Failure(t) =>
      setNextStep[HttpResponse]
      log.error(t, s"Unexpected request error: ${t.getMessage}")
      t match {
        case e: Exception => self ! handleError(e)
        case t: Throwable => throw t
      }

    //the actor didn't receive a method before startTimeout
    case ReceiveTimeout =>
      log.error(
        s"$self didn't receive a next message within ${recvTimeout.toMillis} milliseconds of the last one. next expected message was ${pendingStep.getName}")
      self ! HttpResponse(StatusCodes.ServiceUnavailable)
  }



  /**
   * Continues execution if this resource supports the content type sent in the request, or halts
   * @return an empty Try
   */
  private def ensureContentTypeSupported(): Try[Unit] = {
    request.entity match {
      case Empty => Success()
      case NonEmpty(ct, _) => unsafeSupportedFormats.contentTypes.contains(ct).orHaltWithT(UnsupportedMediaType)
    }
  }

  /**
   * Continues execution if this resource can respond in a format that the requester can accept, or halts
   * @return a Try containing the acceptable content type found, or a failure
   */
  private def ensureResponseContentTypeAcceptable(): Try[ContentType] = {
    request.acceptableContentType(List(unsafeSupportedFormats.responseContentType)).orHaltWithT(NotAcceptable)
  }

  /**
   * Given a matching HTTP response code, add the given header to that response
   * @param response the initial response
   * @param status the response status code
   * @param header the header to conditionally add
   * @return a possibly modified response
   */
  private def addHeaderOnCode(response: HttpResponse, status: StatusCode)
                             (header: => HttpHeader): HttpResponse = {
    if(response.status == status) {
      response.withHeaders(header :: response.headers)
    } else {
      response
    }
  }

  private def handleError(exception: Exception) = {
    exception match {
      case h: HaltException =>
        val response = addHeaderOnCode(h.response, Unauthorized) {
          `WWW-Authenticate`(HttpUtil.unauthorizedChallenge(request))
        }
        val headers = addLanguageHeader(mbSupportedFormats.flatMap(_.responseLanguage), response.headers)
        // If the error already has the right content type, let it through, otherwise coerce it
        val finalResponse = response.withHeadersAndEntity(headers, response.entity.flatMap {
          entity: NonEmpty =>
            entity.contentType match {
              case HttpUtil.errorResponseType => entity
              case _ => HttpUtil.coerceError(entity.data.toByteArray)
            }
        })
        if (finalResponse.status.intValue >= 500) {
          val statusCode = finalResponse.status.intValue
          log.warning(s"Request finished unsuccessfully with status code: $statusCode")
        }
        finalResponse
      case otherException =>
        log.error(s"Unexpected request error: ${otherException.getMessage}", otherException)
        HttpResponse(InternalServerError,
          HttpUtil.coerceError(Option(otherException.getMessage).getOrElse("").getBytes(charsetUtf8)),
          addLanguageHeader(mbSupportedFormats.flatMap(_.responseLanguage), Nil))
    }
  }

  /**
   * Adds a `Content-Language` header to the current header list if the given `responseLanguage` is not None, and the
   * given `headers` list does not yet have a `Content-Language` header set
   * @param responseLanguage the value to assign the `Content-Language` header, or None, if not required
   * @param headers the current list of headers
   * @return augmented list of `HttpHeader` object, or the same list as `response.headers` if no modifications needed
   */
  private def addLanguageHeader(responseLanguage: Option[Language], headers: List[HttpHeader]) : List[HttpHeader] = {
    responseLanguage match {
      case Some(lang) =>
        if (headers.exists(_.lowercaseName == HttpUtil.CONTENT_LANGUAGE_LC)) {
          headers
        } else {
          RawHeader(HttpUtil.CONTENT_LANGUAGE, lang.toString()) :: headers
        }
      case None => headers
    }
  }

}

object HttpResourceActor {

  /*
   * Contract between this and AbstractResourceActor
   */
  /**
   * ResourceContext contains all information needed to start an AbstractResourceActor
   * @param httpActor the HttpActor starting the AbstractResourceActor
   */
  case class ResourceContext(httpActor: ActorRef)

  //requests
  /**
   * Request sent to AbstractResourceActor to request content type and language information
   */
  case object CheckSupportedFormats

  /**
   * Sent to AbstractResourceActor to indicate that a request should be processed
   * @param req The parsed request to process
   */
  case class ProcessRequest(req: Any)

  //responses
  /**
   * Response from AbstractResourceActor containing content type information
   * @param contentTypes Acceptable content types
   * @param responseContentType Content type of the response
   * @param responseLanguage Language of the response
   */
  case class SupportedFormats(contentTypes: List[ContentType],
                              responseContentType: ContentType,
                              responseLanguage: Option[Language])
  case class RequestIsProcessed(response: HttpResponse, mbLocation: Option[String])

  /**
   * the function that parses an [[HttpRequest]] into a type, or fails
   * @tparam T the type to parse the request into
   */
  type RequestParser[T] = HttpRequest => Try[T]

  /**
   * the only message to send each [[HttpResourceActor]]. it begins processing the [[AbstractResourceActor]] that it contains
   */
  object Start

  /**
   * the default receive timeout for most steps in ResourceActor
   */
  val defaultRecvTimeout = 250.milliseconds

  /**
   * the receive timeout for the process function step in ResourceActor
   */
  val defaultProcessRecvTimeout = 2.seconds

  val dispatcherName = "resource-actor-dispatcher"

  /**
   * create the [[Props]] for a new [[HttpResourceActor]]
   * @param resourceActorProps function for creating props for an actor which will handle the request
   * @param reqContext the [[ResourceContext]] to pass to the [[HttpResourceActor]]
   * @param reqParser the parser function to pass to the [[HttpResourceActor]]
   * @param mbResponseActor the optional actor to pass to the [[HttpResourceActor]]
   * @tparam ParsedRequest the type of the parsed request
   * @return the new [[Props]]
   */
  def props[ParsedRequest](resourceActorProps: ResourceContext => AbstractResourceActor,
                           reqContext: RequestContext,
                           reqParser: HttpResourceActor.RequestParser[ParsedRequest],
                           mbResponseActor: Option[ActorRef],
                           processRecvTimeout: Duration = defaultProcessRecvTimeout,
                           recvTimeout: Duration = defaultRecvTimeout): Props = {
    Props.apply(new HttpResourceActor(resourceActorProps, reqContext, reqParser, mbResponseActor, recvTimeout, processRecvTimeout))
      .withDispatcher(dispatcherName)
      .withMailbox("single-consumer-mailbox")
  }

}
