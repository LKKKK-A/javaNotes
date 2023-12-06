ReentrantReadWriteLock:
	- 实现了ReadWriteLock接口：提供两把锁，两把锁共用一个AQS同步器
				    读锁->readLock
				    写锁 -> writeLock
	- 实现了读读并发，读写，写写互斥 ->
				读操作获取的锁是共享锁（Share），获取锁失败后将Thread封装成Node加入
			    阻塞队列时，Node是Share类型
				写操作获取的锁是独占锁（Exclusive）,获取锁失败后将Thread封装成Node加入
			    阻塞队列时，Node是Exclusive类型
	- 加锁解锁大致流程和ReentrantLock的加锁流程差不多，但当当前锁释放后，唤醒阻塞队列中的Node节点如果是	
	Share类型的话，就会继续唤醒后续节点，会把在阻塞队列中连续的Share节点都唤醒，由于读写锁支持读读并发，所以这些节点都会
	获取到锁
	- 特殊的state：32位的state，高十六位用来表示读锁，第十六位表示写锁
		个人理解：读写锁虽然有两把锁，但是共用了一个AQS同步器，所以只能将state分开利用；节约内存
	- 重入时支持锁降级，不支持锁升级

StampedLock: 相比于ReentrantReadWriteLock，它的读性能更好，但功能不如ReentrantReadWriteLock强大，比如不支持多个条件变量，不支持重入
	- StampedLock性能好的原因：他引入了乐观读的思想，在进行读操作的时候我们可以先进行一次乐观读锁的获取（tryOptimisticRead()），
	他会返回一个stamp戳，在真正读取时进行一次验戳，如果失败，说明有写操作执行，这时我们再将乐观读锁升级为悲观锁。
	起始就是再没有写操作干扰的时候，进行读读并发的时候就不用进行cas加锁，进一步提高的读读并发性能。


	