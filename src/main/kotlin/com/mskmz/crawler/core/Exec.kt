package com.mskmz.crawler.core

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//在这里定义exec的相关工具及封装
object ExecUtils {
    //普通任务
    fun ExecAddRun(run: Runnable, exec: ScheduledExecutorService = CoreObj.exec) {
        exec.execute(run)
    }

    //普通任务
    fun ExecAddScheduleRun(run: () -> Unit, exec: ScheduledExecutorService = CoreObj.exec, delay: Long = 5) {
        exec.schedule(run, delay, TimeUnit.SECONDS)
    }


    //循环任务
    fun ExecAddLoopRun(run: () -> Unit, exec: ScheduledExecutorService = CoreObj.exec, delay: Long = 5) {
        ExecAddRun(
            {
                run()
                ExecAddLoopRun(run, exec, delay)
            }, exec
        )
    }

    //条件循环任务
    fun ExecAddIfLoopRun(run: () -> Boolean, exec: ScheduledExecutorService = CoreObj.exec, delay: Long = 5) {
        ExecAddRun(
            {
                if (run()) {
                    ExecAddIfLoopRun(run, exec, delay)
                }
            }, exec
        )
    }
}
