package cn.roamblue.cloud.common.task;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
public class ScheduledExecutor {
    private static ScheduledExecutorService executorService;

    public static void init(int threadSize){
         executorService =new ScheduledThreadPoolExecutor(threadSize, new BasicThreadFactory.Builder().namingPattern("task-executor-pool-%d").daemon(true).build());
    }

    /**
     * 周期执行任务
     * @param runner
     * @param param
     * @param interval
     */
    public static <R,V> void schedule(ScheduleRunner<R,V> runner,R param,int interval){
        executorService.scheduleWithFixedDelay(()->ScheduledExecutor.run(runner,param), interval,interval, TimeUnit.MILLISECONDS);
    }

    /**
     * 立即执行任务
     * @param runner
     * @param param
     */
    public static  <R,V> void submit(ScheduleRunner<R,V> runner,R param){
        executorService.submit(()->ScheduledExecutor.run(runner,param));
    }
    private static  <R,V> void run(ScheduleRunner<R,V> runner,R param){
        try{
            runner.onScheduleFinish(true,param, runner.run(param), null);
        }catch (Throwable err){
            runner.onScheduleFinish(false,param,null,err);
        }
    }






    public interface ScheduleRunner<R,V>{
        /**
         * 执行任务
         * @param param 参数
         * @return 执行任务
         * @throws Throwable
         */
        V run(R param) throws Throwable;

        /**
         * 调用完成
         * @param isSuccess 是否成功
         * @param param
         * @param result 调用结果
         * @param err 异常信息
         *
         */
         void onScheduleFinish(boolean isSuccess,R param,V result,Throwable err);
    }
}
