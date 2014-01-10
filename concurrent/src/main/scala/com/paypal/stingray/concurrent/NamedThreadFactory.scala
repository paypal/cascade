package com.paypal.stingray.concurrent

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import annotation.tailrec

/**
 * Copyright 2012 StackMob Inc.
 *
 * Provide a method to get DefaultThreadFactory instances with a specified thread name prefix.
 * This should be used to make debugging easier.
 *
 * com.stackmob.core
 * 9/4/12
 *
 * @author Will Palmeri <will@stackmob.com>
 */

trait NamedThreadFactory {

  def namedThreadFactory(prefix: String): ThreadFactory = {
    newThreadFactory(defaultThreadGroup(), prefix, false, Thread.NORM_PRIORITY)
  }

  def namedDaemonThreadFactory(prefix: String): ThreadFactory = {
    newThreadFactory(defaultThreadGroup(), prefix, true, Thread.NORM_PRIORITY)
  }

  def namedDaemonRootThreadFactory(prefix: String): ThreadFactory = {
    newThreadFactory(rootThreadGroup(), prefix, true, Thread.NORM_PRIORITY)
  }

  private def newThreadFactory(group: ThreadGroup, prefix: String, isDaemon: Boolean, priority: Int) = new ThreadFactory {
    //DefaultThreadFactory impl (converted to scala) with a custom prefix
    //e.g. http://fuseyism.com/classpath/doc/java/util/concurrent/Executors-source.html
    val threadNumber = new AtomicInteger(1)

    override def newThread(r: Runnable): Thread = {
      val t = new Thread(group, r, "%s-%s" format(prefix, threadNumber.getAndIncrement), 0)
      t.setDaemon(isDaemon)
      t.setPriority(priority)
      t
    }

  }

  private def defaultThreadGroup(): ThreadGroup = Option(System.getSecurityManager) some {_.getThreadGroup } none { Thread.currentThread.getThreadGroup }

  /**
   * Spawned threads have the same thread group as the root group.
   *  This is the magic that will prevent custom code from spawning new threads.
   *
   * See java.lang.SecurityManager.checkAccess(...) for below:
   *
   * ...
   * if (g == rootGroup) {
   * checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
   * } else {
   * // just return
   * }
   * ...
   *
   */
  private def rootThreadGroup(): ThreadGroup = {
    def getRootThreadGroup(node: ThreadGroup): ThreadGroup = {
      Option(node.getParent).map(getRootThreadGroup(_)) | node
    }

    getRootThreadGroup(Thread.currentThread.getThreadGroup)
  }

}
