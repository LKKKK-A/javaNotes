-1.8 cas + synchronized
初始化：懒惰初始化
	- put 时进行初始化
扩容（transfer，扩容时会对正在迁移的桶的头节点加synchronized）（rehash）：多线程并发扩容，再put的时候发现table数组的当前桶的头节点的hash值是-1（此桶位的Node节点已经迁移完毕），
          就会知道table正在扩容，就会帮助扩容。
	- 扩容的时候会为每个线程分配任务（就是指定每个线程对哪些桶的Node进行迁移（默认最小每个线程的处理16个桶），通过
  	  transferIndex（刚开始指向table末尾，每次减16）指定范围）
put: （1）、通过散列函数得到hash值
	- 检查table是否初始化
	- 与（table.length - 1）& hash得到再table中的桶位下标
       （2）、头节点为null，直接通过cas进行put
       （3）、头节点不为null
	- hash值为-1，帮助扩容，否则，对头节点加synchronized，进行链表或红黑树的遍历比较（"=="、"equals()）添加
	- 添加完毕判断是否需要树化
	最后，addCount();
addCount: 类似于LongAdder，通过CounterCell数组进行并发计数（分段CAS）

spread: 

get: (1)、得到对应的桶下标
     （2）、头节点的hash值是否为小于1，若是则从newtable中获取

spread：

1.7 segment + ReentrantLock
	- segment数组再刚开始就初始化，segment数组里面的HashEntry数组是懒惰初始化
	- 确定key所在的下标，需要两次的计算：
		first：通过hash & (segment.length - 1)确定在哪个segment中
		second：通过hash & (HashEntry.length - 1)确定在哪个桶中



	
