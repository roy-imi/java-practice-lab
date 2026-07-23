package com.roy.futurelab.exercises;

import com.roy.futurelab.common.DemoServices;
import com.roy.futurelab.model.ProductSummary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/*
 * 第 1 课：用 Future 同时查询价格与库存。
 *
 * 【背景知识：从同步调用到 Future】
 *
 * 普通方法调用会在当前线程执行，方法没有返回前，当前线程无法继续处理后面的代码。
 * 当价格查询和库存查询来自两个互不依赖的远程服务时，串行等待会浪费时间：
 * 总耗时大致是两个服务耗时之和。把它们交给线程池后，两个任务可以重叠执行，
 * 总耗时更接近较慢的那个任务。
 *
 * ExecutorService 负责接收和调度任务。调用 submit() 时，得到的 Future
 * 不是任务结果本身，而是一个“未来结果的句柄”。submit() 通常很快返回，
 * 真正的工作由线程池中的工作线程执行。
 *
 * 【本课会用到的 Future 能力】
 *
 * - get()：取得任务结果；任务未完成时，调用 get() 的线程会阻塞。
 * - isDone()：查看任务此刻是否结束；它只是状态快照，不会等待任务。
 * - submit(Callable)：提交有返回值的任务，得到对应的 Future。
 *
 * 【API 详解】
 *
 * 1. executor.submit(task)
 *
 * - task：一个 Callable，也就是“无参数、有返回值、允许抛异常”的任务。
 *   Lambda 表达式 () -> 某个返回值方法，正好可以表示 Callable。
 * - 返回值：Future，Future 中保存的结果类型与 Callable 的返回值类型相同。
 * - 调用效果：只负责提交任务，通常不会等待任务执行完成。
 *
 * 2. future.get()
 *
 * - 参数：没有参数。
 * - 返回值：Callable 最终计算出的结果。
 * - 调用效果：结果尚未产生时，阻塞当前调用线程。
 * - 可能异常：当前线程被中断时抛 InterruptedException；
 *   任务内部失败时抛 ExecutionException。
 *
 * 3. future.isDone()
 *
 * - 参数：没有参数。
 * - 返回值：boolean。任务正常完成、异常结束或被取消时都会返回 true。
 * - 注意：true 只代表“结束”，不代表“一定成功”。
 *
 * 4. trace.accept(message)
 *
 * - message：要交给 Consumer 的一条字符串消息。
 * - 返回值：没有返回值。
 * - 本课用途：把 Future 的状态交给外部展示，而不是在业务方法中写死输出方式。
 *
 * 【关键思路】
 *
 * “先提交全部独立任务，再等待结果”是本课最重要的原则。
 * 如果提交第一个任务后立刻 get()，直到它完成才提交第二个任务，
 * 代码虽然使用了 Future，执行效果仍然接近串行。
 *
 * 【线程池所有权】
 *
 * 谁创建线程池，通常由谁关闭。当前方法只是借用调用方传入的 executor，
 * 因此不应在方法内部调用 shutdown()，否则可能影响共享该线程池的其他任务。
 *
 * 约束：
 * 1. 必须先提交两个任务，再等待任意一个结果；
 * 2. 用 trace 输出两个 Future 刚提交后的 isDone 状态；
 * 3. 不要在这里关闭 executor，它由调用方创建并关闭。
 */
public final class Lab01FutureBasics {
    private Lab01FutureBasics() {
    }

    public static ProductSummary loadProductSummary(DemoServices services,
                                                    ExecutorService executor,
                                                    String productName,
                                                    Consumer<String> trace)
            throws Exception {
        /*
         * TODO 你的任务：
         * - 把 queryPriceCents 和 queryStock 分别提交给 executor；
         * - 观察 Future<Integer>；
         * - 两个任务都提交后，再通过 Future 取得结果；
         * - 组装并返回 ProductSummary。
         *
         * 先运行本课，再按 docs/hints/lab-01.md 逐级查看提示。
         */

        Future<Integer> priceFuture = executor.submit(() -> services.queryPriceCents(productName));
        Future<Integer> stockFuture = executor.submit(() -> services.queryStock(productName));

        int priceCent = priceFuture.get();
        int stock = stockFuture.get();

        return new ProductSummary(productName, priceCent, stock);
    }
}
