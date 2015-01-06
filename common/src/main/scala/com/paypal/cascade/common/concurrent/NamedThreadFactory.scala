/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.concurrent

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Provide a method to get DefaultThreadFactory instances with a specified thread name prefix.
 * This should be used to make debugging easier.
 *
 * The root thread is the top-most thread of the current JVM. A method is provided here to obtain a factor that
 * creates daemon children directly under that root thread.
 *
 * Refer to documentation on `java.lang.Thread` for distinctions between daemon/non-daemon threads.
 * Short version: daemon threads die immediately when their parent dies; non-daemon threads are allowed to finish.
 */
trait NamedThreadFactory {

  /**
   * Returns a new named non-daemon thread factory at normal thread priority.
   * @param prefix the name prefix for threads of this factory
   * @return a new ThreadFactory
   */
  def namedThreadFactory(prefix: String): ThreadFactory = {
    newThreadFactory(defaultThreadGroup(), prefix, false, Thread.NORM_PRIORITY)
  }

  /**
   * Returns a new named daemon thread factory at normal thread priority.
   * @param prefix the name prefix for threads of this factory
   * @return a new ThreadFactory
   */
  def namedDaemonThreadFactory(prefix: String): ThreadFactory = {
    newThreadFactory(defaultThreadGroup(), prefix, true, Thread.NORM_PRIORITY)
  }

  /**
   * Returns a new named daemon thread factory, for daemon threads under the root thread, at normal thread priority.
   * @param prefix the name prefix for threads of this factory
   * @return a new ThreadFactory
   */
  def namedDaemonRootThreadFactory(prefix: String): ThreadFactory = {
    newThreadFactory(rootThreadGroup(), prefix, true, Thread.NORM_PRIORITY)
  }

  private def newThreadFactory(group: ThreadGroup, prefix: String, isDaemon: Boolean, priority: Int) = new ThreadFactory {
    //DefaultThreadFactory impl (converted to scala) with a custom prefix
    //e.g. http://fuseyism.com/classpath/doc/java/util/concurrent/Executors-source.html
    val threadNumber = new AtomicInteger(1)

    override def newThread(r: Runnable): Thread = {
      val t = new Thread(group, r, s"$prefix-${threadNumber.getAndIncrement}", 0)
      t.setDaemon(isDaemon)
      t.setPriority(priority)
      t
    }

  }

  private def defaultThreadGroup(): ThreadGroup = Option(System.getSecurityManager) match {
    case Some(sm) => sm.getThreadGroup
    case None => Thread.currentThread.getThreadGroup
  }

  /**
   * Returns the root thread group.
   * @return the root thread group
   */
  private def rootThreadGroup(): ThreadGroup = {
    def getRootThreadGroup(node: ThreadGroup): ThreadGroup = {
      Option(node.getParent).map(getRootThreadGroup).getOrElse(node)
    }
    getRootThreadGroup(Thread.currentThread.getThreadGroup)
  }

}
