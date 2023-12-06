## 为什么aof的速度会慢于rdb?



AOF（Append-Only File）和RDB（Redis Database）是Redis持久化机制中两种常见的方法。AOF方式将所有写操作以追加的方式写入磁盘，而RDB方式则是通过定期生成快照来保存数据。

尽管AOF和RDB都可以用于数据持久化，但它们的速度可能会有所不同，主要有以下几个原因：

1. 写入方式：AOF是将每个写操作追加到文件末尾，这意味着每个操作都要写入磁盘。相比之下，RDB是生成整个数据集的快照并保存到磁盘，这意味着它不需要频繁地写入磁盘。因此，从写入操作的角度来看，AOF的速度通常会慢于RDB。

2. 文件大小：AOF文件通常会随着时间的推移逐渐增大，因为每个写操作都会追加到文件末尾。当AOF文件变得非常大时，写入性能可能会下降，因为操作系统需要处理更大的文件。相比之下，RDB文件的大小相对较小，因为它只是一个数据集的快照。因此，在处理大型AOF文件时，速度可能会慢于RDB。

3. 数据恢复：由于AOF文件保存了所有的写操作，Redis在启动时需要重新执行AOF文件中的所有写操作来恢复数据。这可能需要更多的时间，尤其是当AOF文件较大时。相比之下，RDB文件只需要加载整个数据集的快照，因此数据恢复速度通常会比AOF快。

需要注意的是，AOF和RDB的选择应该基于具体的使用场景和需求。AOF相对于RDB提供了更高的数据安全性，因为它记录了所有的写操作，但这也导致了更高的写入延迟和更大的文件大小。而RDB则提供了更快的恢复速度和更小的文件大小，但在发生故障时可能会有一定程度的数据丢失。因此，根据应用程序的需求和性能要求，选择适合的持久化方式是很重要的。

## 为什么rdb的快照速度快？



RDB的快照速度相对较快，主要有以下几个原因：

1. 数据格式：RDB使用了一种紧凑的二进制格式来保存数据集的快照。这种格式是经过优化的，只包含实际的数据和少量的元数据，例如键、值、过期时间等。相比之下，AOF以文本形式保存每个写操作的命令，需要更多的磁盘空间和写入操作。由于RDB文件的格式更为紧凑，保存和加载数据的速度较快。

2. 单次操作：RDB通过生成数据集的一次快照来保存数据。生成快照的过程是单次操作，Redis会将当前数据集的副本写入磁盘。这种方式不需要频繁地写入磁盘，因此在性能上较为高效。

3. IO操作：生成RDB快照通常涉及到的IO操作相对较少。一旦生成了RDB文件，Redis不会频繁地对其进行写入操作。相比之下，AOF需要将每个写操作追加到文件末尾，这可能导致更多的IO操作和磁盘访问延迟。

4. 数据恢复：RDB文件作为整个数据集的快照，可以直接加载到内存中进行恢复。加载RDB文件只需要读取文件并还原数据集，相对来说速度较快。而AOF文件在恢复时需要逐条执行保存的写操作，这可能需要更多的时间。

需要注意的是，尽管RDB的生成和加载速度通常较快，但它可能会导致较长的数据恢复时间间隔和更多的数据丢失。因为RDB是定期生成的快照，如果Redis在生成快照之间发生故障，那么在最后一次快照之后的数据将会丢失。因此，在选择RDB作为持久化方式时，需要根据应用程序的需求和对数据丢失的容忍度进行权衡。



[面试必问的 Redis：RDB、AOF、混合持久化 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/340082703)

## AOF 后台重写存在的问题



AOF 后台重写使用子进程进行从写，解决了主进程阻塞的问题，但是仍然存在另一个问题：子进程在进行 AOF 重写期间，服务器主进程还需要继续处理命令请求，新的命令可能会对现有的数据库状态进行修改，从而使得当前的数据库状态和重写后的 AOF 文件保存的数据库状态不一致。

## 如何解决 AOF 后台重写存在的数据不一致问题



为了解决上述问题，Redis 引入了 AOF 重写缓冲区（aof_rewrite_buf_blocks），**这个缓冲区在服务器创建子进程之后开始使用，当 Redis 服务器执行完一个写命令之后，它会同时将这个写命令追加到 AOF 缓冲区和 AOF 重写缓冲区**。

这样一来可以保证：

1、现有 AOF 文件的处理工作会如常进行。这样即使在重写的中途发生停机，现有的 AOF 文件也还是安全的。

2、从创建子进程开始，也就是 AOF 重写开始，服务器执行的所有写命令会被记录到 AOF 重写缓冲区里面。

这样，当子进程完成 AOF 重写工作后，父进程会在 serverCron 中检测到子进程已经重写结束，则会执行以下工作：

1、将 AOF 重写缓冲区中的所有内容写入到新 AOF 文件中，这时新 AOF 文件所保存的数据库状态将和服务器当前的数据库状态一致。

2、对新的 AOF 文件进行改名，原子的覆盖现有的 AOF 文件，完成新旧两个 AOF 文件的替换。

之后，父进程就可以继续像往常一样接受命令请求了。

## BigKey

### 通过分批次删除，为什么可以让删除操作与其他操作交替执行?



通过分批次删除，可以让删除操作与其他操作交替执行的`主要原因是Redis的单线程特性。`

在Redis中，所有的命令操作都是按顺序执行的，一个命令执行完成后才能执行下一个命令。当执行一个耗时较长的命令（如删除大key）时，Redis主进程会被阻塞，无法处理其他命令请求。

*通过将删除操作分批次执行，可以让每个批次的删除操作执行的时间较短，从而减少了每次删除的阻塞时间。在每个删除批次之间，Redis主进程有机会处理其他的命令请求，包括读取、写入和其他操作。*

这样就实现了删除操作与其他操作的交替执行。通过合理设置每个批次的删除数据量和删除间隔，可以让系统在删除大key的同时保持较好的响应性能，提高整体的吞吐量和并发处理能力。

需要注意的是，虽然分批次删除可以减少删除操作的阻塞时间，但是仍然会有一定的性能影响。在删除大量数据时，仍然需要考虑系统的负载和响应时间，以避免过度消耗资源或影响用户体验。因此，需要根据实际情况进行测试和调优，找到适合系统的最佳批次大小和删除间隔。



## redis缓存击穿和缓存穿透

**缓存穿透**指的是数据库本就没有这个数据，请求直奔数据库，**缓存**系统形同虚设。

**缓存击穿**(失效)指的是数据库有数据，**缓存**本应该也有数据，但是**缓存**过期了，Redis 这层流量防护屏障被**击穿**了，请求直奔数据库。

缓存击穿
对于一些设置了过期时间的key，如果这些key可能会在某些时间点被超高并发地访问，是一种非常“热点”的数据。这个时候，需要考虑一个问题：缓存被“击穿”的问题，这个和缓存雪崩的区别在于这里针对某一key缓存，前者则是很多key。

缓存在某个时间点过期的时候，恰好在这个时间点对这个Key有大量的并发请求过来，这些请求发现缓存过期一般都会从后端DB加载数据并回设到缓存，这个时候大并发的请求可能会瞬间把后端DB压垮。

原文链接：https://blog.csdn.net/zeb_perfect/article/details/54135506



## redis数据结构

String: SDS

​		3大物理编码方式：int 、 embstr、 row	

​				int: 保存 long 型的64位的有符号整数

​				embstr: 保存小于44字节的字符串

​				row: 保存长度大于44字节的字符串

​		为什么要设计SDS？

​				①字符串长度处理，不用遍历，直接读取O(1)

​				②空间预分配

​				③惰性空间释放

​				④二进制安全（'\0'）

hash:  

​		hash对象保存的**键值对的数量小于512个**，所有键值对的**键和值的字符串长度都小于等于64byte**(一个英文字母一个字节)时用 ziplist ( listpack )，反之用 hashtable

​		redis6:    ziplist + hashtable

​		redis7:    listpack + hashtable

list: 底层是quicklist

​		redis6:    (quicklist)  ziplist + linkedlist

​		redis7:    (quicklist)  listpack + linkedlist

set:

​		intset + hashtable

zset:

​		redis6:    ziplist + skiplist

​		redis7:    listpack + skiplist

​		skiplsit: 解决普通 list 遍历的比较慢的问题O(N)

​						skiplist 是可以实现二分查找的有序列表，通过给列表加索引（空间换时间）O（logN）

​						

## HyperLogLog 算法的原理讲解以及 Redis 是如何应用它的

​		https://juejin.cn/post/6844903785744056333#comment

## 彻底搞懂 Redis 主从复制机制

[彻底搞懂 Redis 主从复制机制 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/151740247)

## Redis — 主从集群切换数据丢失问题

[Redis — 主从集群切换数据丢失问题_RachelHwang的博客-CSDN博客](https://blog.csdn.net/qq_38658567/article/details/116163829#:~:text=如果此时master还没来得及同步给slave节点时发生宕机，那么master内存中的数据会丢失；要是master中开启持久化设置数据可不可以保证不丢失呢？,答案是否定的。)



dictEntry.key , dictEntry.val  ->  redisObject ,redisObject.ptr   ->  redis数据类型（String、list、set、zset、hash....） -> 底层实现（redis数据结构）  ->  (skiplist、sds、linkedlist、ziplist、hashtable、inset、quicklist、listpack) 

## listpack如何遍历？如何解决ziplist连锁更新问题？

[listpack如何遍历？如何解决ziplist连锁更新问题？(五)_小派师兄的博客-CSDN博客](https://blog.csdn.net/weixin_45735834/article/details/126559632)

## redis 高性能的原因（redis为什么这么快）

[Redis高性能原理：Redis为什么这么快？_redis高效原理_Java后端何哥的博客-CSDN博客](https://blog.csdn.net/CSDN2497242041/article/details/120755188)

## 深入理解Redis跳跃表的基本实现和特性

https://juejin.cn/post/6893072817206591496



## redis是单线程还是多线程

redis它对于命令的处理这一部分一直采用的单线程（work线程）

就redis整体来说，采用的是多线程

其实在redis4.0版本就引入了多线程，比如对于一些大key的删除，redis提供了unlink命令来异步删除大key，避免主线程的阻塞

redis6.0之后又在网络模型中引入了多线程（IO线程）。 



在redis6之前的版本，redis内核它采用的是单线程，也就是只有一个work线程来处理来自客户端的命令请求，在redis6及之后的版本中，redis引入了多线程的概念，但这里的多线程并不是值redis内核中有多个work线程来处理读写请求，而是指多个IO子线程，当接收到redis客户端的读写请求后，并不像redis5那样，对读写命令的解析，处理，结果回写整个过程都交给work线程来做，而是把对命令的解析和处理结果回写的过程交给IO子线程来做，对命令的处理还是有一个work线程来做。

我们知道redis它是基于内存的数据库，命令处理非常快，造成redis性能瓶颈主要是内存大小和网络IO，而内存大小主要取决于物理硬件，所以就引入多个IO线程来降低网络IO对redis的影响。用单线程来来对命令进行处理，一方面降低了redis的设计复杂度，又避免了多线程环境的线程安全问题。

## IO多路复用函数

**select模式：**

用户线程在调用select函数之前，首先会创建一个fd数组，将需要监听的socket 对应的bit位置为1，然后把该fd数组作为参数传给select函数，内核去执行select函数，遍历一边fd数组，找出需要监听的socket，当socket 有数据后就将准备好数据的socket个数返回给用户线程，由于返回的是准备好数据的socket个数，并没有指明具体那几个socket准备好，所以还需要将fd数组拷贝到用户空间，由用户线程来遍历socket 来读取数据。

缺点：

- fd数组优先，只能监听1024个socket连接
- 只返回了准备好数据的socket个数，并没有指明具体的socket
- 涉及到fd数组在用户空间和内核空间的拷贝

> fd数组：是一个大小为32和 long int 数组，long int 占32位，所以总共占1024bit

**poll模式：**

poll函数相比于select函数来说，并没有很大的改进，只是做了一些小优化。

pollfd结构体中有三个属性：

	

```c
struct pollfd {
    int fd; // 要监听的fd
    short events; // 要监听的事件类型
    short revents; // 实际发生的事件类型
}
```

**poll函数不在使用比特位来表示fd，而是用用pollfd结构体的数组来表示要监听的socket连接**，客户端在调用poll函数之前将pollfd将要监听的每一个socket的fd封装在pollfd中，拷贝到内核空间，内核对pollfd中指定的socket 进行监听，若有对应的socket准备好数据，就将对应的pollfd中的实际发生的事件类型属性进行填充，这个字段默认是0。然后向客户端返回就绪的socket的数量，和select函数一样，并没有指明具体已就绪的socket，所以poll函数还是要将pollfd数组拷贝到用户空间。客户端还是要遍历pollfd数组来找哪些fd有数据。

相比于select，poll函数的优化就是监听的fd没有上限。

**epoll模式：**

相比于select和poll模式，epoll模式有很大的改进。

epoll模式它分为三部分：epoll_create、epoll_ctl、epoll_wait

**epoll_create ( int size )**：它会在内核创建eventpoll结构体，这个结构体中主要有两部分，红黑树和就绪链表。

```c
struct eventpoll{
	struct rb_root rbr; // 一棵红黑树，用来存放要监听的fd
    struct list_head;  // 一个链表，用来存放就绪的fd
};
```

**epoll_ctl ( int epfd,int op,int fd,struct epoll_event * event )**：将要监听的fd添加到红黑树中,并对fd绑定callback()回调函数，当fd就绪后会执行回调函数，把fd加入到eventpoll结构体中的就绪链表中

```c
epoll_ctl(
	int epfd;  // eventpoll的句柄
    int op; // 要对红黑树进行的操作，增删改
    int fd; // 要添加的fd
    struct epoll_event *event;  // 要监听的事件类型：读事件、写事件、异常事件等 
)
```

**epoll_wait ( int epfd,struct epoll_event *event,int maxevents,int timeout )**：首先会检查就绪链表是否为空，若不为空，返回就绪就绪的fd的数量，并把就绪链表中的fd拷贝到用户空间的event数组中；若为空的话，进行等待

```c
epoll_wait(
    int epfd; // eventpoll句柄
    struct epoll_event *events; // 空的event数组，用来接受就绪的fd
    int maxevents; // events数组的最大长度
    int timeout; // 等待超时时间
)
```

**epoll模式相比于select和poll模式的优点：**

- 真正的实现了可以监听无限多个fd，poll模式虽然理论上是可以实现无限监听的，但由于是链表的方式，若是监听大量的fd，那么在遍历的时候会很耗时，所以一般会控制监听的数量，但是epoll模式采用了红黑树来存储要监听的fd，相比于链表，红黑树的性能会高很多，而且epoll模式中，一般不会对红黑树进行遍历，只是做增删改的操作。
- 对于同一个fd，select和poll在每次的请求监听时，都会在用户态和内核态之间进行拷贝，而epoll模式对于相同的fd只会拷贝一次，之后回一直存储在内核的红黑树中，减少了大量的重复的拷贝。
- epoll模式在想用户空间返回结果时并不会将所有的fd都拷贝，而是将就绪的fd拷贝到用户空间的数组中，既减少了拷贝的开销和用户线程在遍历时只遍历就绪的fd。

![image-20231201175118156](C:\Users\李凯\AppData\Roaming\Typora\typora-user-images\image-20231201175118156.png)



## redis网络模型

![image-20231201191000958](C:\Users\李凯\AppData\Roaming\Typora\typora-user-images\image-20231201191000958.png)



aeEventLoop：相当于epoll中的epoll_ctl，将要监听的fd进行注册

beforeSleep：监听fd写事件，绑定写处理器。当client队列中有数据，说明有客户端的请求处理好了，就会交给`命令回复处理器`给客户端响应结果

asApiPoll：相当与epoll_wait，监听fd的读事件（serverSocket的fd和clientSocket的fd是否可读），如果有fd可读的话，判断是那种类类型：serverSocket可读的话，说明有客户端的连接，就会交给`连接应答处理器`来处理，也就是执行accept()，获得客户端的socket的fd，对fd进行一个注册监听；如果clientSocket可读的话，就交给`命令请求处理器`，对客户端的命令进行读取、解析、执行、将执行结果写入客户端队列中。

> 客户端队列会有beforeSleep函数来进行遍历，队列中有数据的话就会由命令回复处理器对队列中的结果进行响应，写到clientSocket中

![啊发生的](C:\Users\李凯\AppData\Roaming\Typora\typora-user-images\image-20231201190837876.png)



redis4.0引入多线程

**第一个就是在命令请求处理器对命令的读取、解析这一部分引入了多线程**，因为这一部分涉及到了网络IO，在读取请求参数的过程中，redis的work线程只能等待，引入多线程后多这部分可以并发处理

**第二个就是命令回复处理器对客户端队列中的数据写入到clientSocket中这部分**，同理，在像客户端响应数据时涉及到网络IO，引入多线程提增加并发度





![](C:\Users\李凯\AppData\Roaming\Typora\typora-user-images\image-20231201190804026.png)



## redis数据结构

### SDS

虽然redis是用c语言写的，但是redis它并没有直接使用c的字符串，因为c的字符串它存在一些问题”

- 二进制安全问题：c语言字符串以'\0'来作为结束标识，所以我们不能存储特殊字符
- 获取字符串长度比较麻烦：要通过遍历或者运算来得到字符串长度
- 对于一些字符串字面量它是直接存储在常量池中的，不能修改

由于c语言中字符串存在这些问题，所以redis它实现了自己的字符串，SDS，简单动态字符串

SDS它有以下优点：

- SDS它其实是一个结构体，里面封装了一些header属性和具体的字符串值。其中header属性包括存储的字符串的字节数，为字符串分配的字节数，和SDS的类型。所以我们在获取字符串的长度的时候，直接从SDS结构体中拿就行了，不用进行遍历或者计算。还有就是不用担心二进制安全的问题，我们可以存志'\0'这样的特殊字符了，因为在进行遍历的时候，我们不在根据'\0'结束字符来遍历，而是根据长度来遍历。
- SDS通过**空间预分配和惰性释放来减少空间重新分配的次数**：因为空间分配是需要在内核态中进行，这涉及到CPU状态的切换，SDS它会对**空间进行预分配**，当SDS的长度变化需要进行空间重新分配时，如果新字符串的长度小于1M，那么重新分配的空间就是新字符串长度的两倍，如果新字符串的长度大于1M，分配的空间就是新字符串的长度再加1M。**空间惰性释放**，在SDS长度所见的时候，并不会立即对空间进行释放，而是进行标记，来重复使用。

### IntSet

inset它是一个整数集合，专门用来存放整数的，redis中的set数据类型当它的元素全部是整数的时候，它内部使用的时intset来存储的，并且并不是无序的，intset它内部是按升序存储的。 

intset具备长度有序、支持编码方式升级等特性

底层也是一个结构体，结构体中由三个属性

- 数组元素编码方式，因为intset底层使用数组来进行存储的。有三种编码方式，支持16位，32位，64位的整数
- 元素个数
- 用来存储数据的数组

intset它可以根据存储整数的字节大小对编码方式进行升级，编码方式并不是固定的。比如intset中的所有整数所占字节数都小于等于2字节，这时intset中的编码方式就是16位的编码方式，若要加入一个大于2字节的整数，这时就要对编码方式进行升级，这里的升级要对整个数组中的每一个元素所占字节数进行重新分配，不能只针对与刚加入的整数，因为redis它底层的数据结构追求高效的增删改查，整体升级有利于用索引快速的定位元素的位置。

intst它在对整数进行存储的时候会利用二分查找，快速的找到该元素应该插入的位置。

### Dict

redis中对于一些键值数据的存储使用的是Dict这种数据结构来存储的

Dict它包含三部分：哈希表（DictHashTable）、哈希节点（DictEntry）、字典（Dict

```c

typedef struct dictht{
    // entry数组，数组中保存的是dictEntry的指针
    dictEntry **table;
    // 哈希表的大小,size的值都是2的n次方，方便利用&运算来确定key在entry中的位置
    unsigned long size;
    // 哈希表大小的掩码,size - 1
    unsigned long sizemark;
    // entry 的个数(entry 的个数和哈希表的大小相等，因为存在hash冲突，会导致时机entry的个数大于哈希表的大小)
    unsigned long used;
}dictht;
```



```c
typedef struct dictEntry{
    void *key; // 键
    union{
        void *val;
        uint64_t u64;
        uint64_t s64;
        double d;
    }v; // 值
    // 指向下一个entry的指针
    struct dictEntry *next;
}dictEntry;
```



```c
typedef struct dict{
    dictType *type; // dict的类型，内置不同的hash函数
    void *privdata; // 私有数据，在做特殊hash运算时用
    dictht ht[2]; // 一个Dict包含两个哈希表，其中一个是用来存储当前数据，另一个一般是空的，rehash时使用
    long rehashidx; // rehash 的进度，-1表示未进行
    int16_t pauserehash; // rehash 是否暂停，1则暂停，0则继续
}
```

####  Dict 的渐进式rehash

因为存在hash碰撞，所以随着Dict中存储的元素越来越多，产生hash碰撞的概率也会越来越大，这样就会导致entry数组中一个角标会存储不止一个元素，这些元素会以链表的方式存储，这样的话就会使得哈希表的增删改查效率降低，这时就不得不对entry数组进行扩容来减少hash碰撞。扩容时会创建容量更大一个新的哈希表，这时哈希表的size和sizemark都会改变，所以要对旧哈希表中的数据重新根据sizemark来计算索引位置，也就是rehash。这个新哈希表会存储在Dict中第二个哈希表中，用来进行rehash

rehash要对哈希表中的所有元素进行迁移，这样存在一个问题，redis它的命令执行是单线程的，如果哈希表中有大量的数据的话，一次性要对所有数据进行迁移,会导致redis的work线程等待很长时间，所以redis采用了渐进式rehash，没有一次性对所有的数据进行迁移，而是在每次对哈希表进行增、删、改、查的时候，只对一个桶位的数据进ZZz行迁移，这样就不会有性能降低，每次进行rehash后会将rehash的进度，也就是rehash到哪个索引位置了，将索引位置存储来Dict中的rehashidx变量中。下次rehash时从该位置接着rehash即可。

redis的rehash其实和ConcurrentHashMap很像，只不过ConcurrentHashMap它可能会在多线程环境下进行rehash，只有在多个线程put的时候，若是发现在扩容，就会帮助扩容，由于是并发环境，要提前位每个线程分配任务，默认时每个线程负责16个桶位的数据迁移。

### ZipList

ziplist它其实类似于双端列表，能从后向前遍历，又能从前向后遍历，只不过和一般的双端链表不同的是，**ziplist并没有使用指针连接每一个节点，并且ziplist它是一块连续的内存空间。**

ziplist它的结构设计，总体上分为header部分和存储内容部分，内容部分也就是一个个的entry节点。header部分主要包括ziplist整体所占的字节大小、从头部到最后一个节点所占的字节大小、还有entry节点的长度大小，第二个属性，也就是到最后一个节点的字节数大小，通过这个属性，在知道ziplist的起始位置前提下，可以快速定位到最后一个节点的位置，从而实现从后往前遍历。

ziplist没有指针，它是怎么实现节点之间的关联的呢？

这主要借助与ziplist它的entry节点的内部实现。entry节点的内部结构包括三部分：前一个节点的字节大小（previous_entry_length）、编码格式(encoding)、和内容部分(content)。因为entry中保存了前一个节点的字节大小，所以可以通过它来得到相邻节点的起始位置。

**对于previous_entry_length这一属性，它占用的字节数并不是固定的**。ziplist考虑到对内存的节省，刚开始只用一个字节来记录前一个节点的字节大小。就是如果前一个节点的占用的字节大小小于254字节的话，就用一个字节来记录；如果大于254个字节的话，就用5个字节来记录前一个节点的大小，也正是因为这样的设计，使得ziplist存在连锁更新的问题。

**连锁更新**

因为每个entry节点的previous_entry_length记录了前一个节点的字节大小，**如果存在连续的entry的所占字节大小都在250-253之间**，这时候记录他们的previous_entry_length这个属性就只占一个字节，如果在某个节点之前新增了一个entry节点，并且这个entry的字节大小大于254个字节，这时，它的后一个节点的pervious_entry_length就要用五个字节来记录上一个节点的字节大小，由于ziplist它是一块连续的内存空间，其中一个节点重新分配内存大小的话就会导致后面的entry整体后移，如果存在很多个连续的在250-253字节的entry的话，这种现象就会连锁发生，**导致频繁的数据移动，和cpu在内核态和用户态之间的频繁切换，因为要分配内存嘛，这样会导致ziplist的性能急速下降。**

**ziplist它不适合用来存储大量的数据**，因为他虽然节省了内存空间，但查找元素的时候，还是通过遍历来查找，数据特别大的时候增删改查的效率不是很高。**所以ziplist它的header中，记录entry大小的属性只占两个字节**，也就是如果ziplist的entry个数超过两个字节的话，这个字段就没法记录了，要想在获取entry的个数就只能遍历了。







