import java.util.LinkedList;

public class ThreadPool extends ThreadGroup {
	private boolean isClosed = false;				//线程池是否关闭
	private LinkedList<Runnable> workQueue;			//表示工作队列
	private static int threadPoolID;				//表示线程池ID
	private int threadID;							//表示工作线程ID
	
	public ThreadPool(int poolSize) {					//poolSize 指定线程池中的工作线程的数目
		super("ThreadPool-" + (threadPoolID++));
		setDaemon(true);
		workQueue = new LinkedList<Runnable>();			//创建工作队列
		for (int i = 0; i < poolSize; i++)
			new WorkThread().start();					//创建并启动工作线程
	}
	
	/** 向工作队列中加入一个新任务, 由工作线程去执行该任务 */
	//synchronized 表示这个方法加锁, 不管哪个线程, 运行到这个方法时, 都要检查有没有其他线程在进行这个方法, 有的话要等该进程运行完再运行
	public synchronized void execute(Runnable task) {
		if (isClosed) {									//线程池被关闭 则抛出 IllegalStateException 异常
			throw new IllegalStateException();
		}
		if (task != null) {
			workQueue.add(task);
			/* 
				唤醒正在等待对象监视器的单个线程。 如果任何线程正在等待这个对象，其中一个被选择被唤醒。 
				选择是任意的，并且由实时的判断发生。 线程通过调用wait方法之一等待对象的监视器。 
				唤醒的线程将无法继续，直到当前线程放弃此对象上的锁定为止。 
				唤醒的线程将以通常的方式与任何其他线程竞争，这些线程可能正在积极地竞争在该对象上进行同步; 
				例如，唤醒的线程在下一个锁定该对象的线程中没有可靠的权限或缺点。 

				该方法只能由作为该对象的监视器的所有者的线程调用。 线程以三种方式之一成为对象监视器的所有者： 

				通过执行该对象的同步实例方法。 
				通过执行在对象上synchronized synchronized语句的正文。 
				对于类型为Class的对象，通过执行该类的同步静态方法。 
				一次只能有一个线程可以拥有一个对象的显示器。 

				异常 
					IllegalMonitorStateException - 如果当前线程不是此对象的监视器的所有者。 
			 */
			notify();					//唤醒正在getTask()方法中等待任务的工作线程
		}
	}
	
	/** 从工作队列中取出一个任务, 工作线程会调用此方法 */
	protected synchronized Runnable getTask() throws InterruptedException {
		while (workQueue.size() == 0) {
			if (isClosed)
				return null;
			/*
				此方法使当前线程（称为T ）将其放置在该对象的等待集中，然后放弃对该对象的任何和所有同步声明。 
				线程T变得禁用线程调度目的，并且休眠，直到发生四件事情之一： 

				1. 一些其他线程调用该对象的notify方法，并且线程T恰好被任意选择为被唤醒的线程。 
				2. 某些其他线程调用此对象的notifyAll方法。 
				3. 一些其他线程interrupts线程T。 
				4. 指定的实时数量已经过去，或多或少。 然而，如果timeout为零，则不考虑实时，线程等待直到通知。 
			*/
			wait();						//如果工作队列中没有任务, 就等待任务
		}
		return workQueue.removeFirst();
	}
	
	/** 关闭线程池 */
	public synchronized void close() {
		if (!isClosed) {
			isClosed = true;
			workQueue.clear();			//清空工作队列
			interrupt();				//中断所有的工作线程, 该方法继承自 ThreadGroup 类
		}
	}
	
	/** 等待工作线程把所有工作执行完, 再关闭线程池 */
	public void join() {
		synchronized (this) {
			isClosed = true;
			notifyAll();				//唤醒还在 getTask() 方法中等待任务的工作线程
		}
		
		Thread[] threads = new Thread[activeCount()];
		//enumerate() 方法继承自 ThreadGroup 类, 获得线程组当中所有活着的工作线程
		int count = enumerate(threads);
		for (int i = 0; i < count; i++) {				//等待所有工作线程运行结束
			try {
				threads[i].join();						//等待工作线程运行结束
			} catch (InterruptedException ex) {}
		}
	}
	
	/** 内部类: 工作线程 */
	private class WorkThread extends Thread {
		public WorkThread() {
			//加入到当前ThreadPool线程组中
			super(ThreadPool.this, "WorkThread-" + (threadID++));
		}
		
		public void run() {
			while (isInterrupted()) {			//isInterrupted()方法继承自Thread类, 判断线程是否被中断
				//继承了Runnable接口的task
				Runnable task = null;
				try {							//取出任务
					task = getTask();
				} catch (InterruptedException ex) {}
				
				//如果getTask()返回null 或者 线程执行getTask()时被中断, 则结束此线程
				if (task == null)
					return;
				
				try {							//运行任务  异常在catch代码块中捕获
					task.run();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}			//#while
		}				//#run()
	}					//#WorkThread类
}