## SQLite数据库
### 创建数据库
Android为了让我们能够更加方便地管理数据库，专门提供了一个SQLiteOpenHelper帮助类，SQLiteOpenHelper是一个抽象类，如果想要使用它，就需要创建一个自己的帮助类去继承它。SQLiteOpenHelper中有两个抽象方法onCreate()和onUpgrade()。我们必须在自己的帮助类里重写这两个方法，然后分别在这两个方法中实现创建和升级数据库的逻辑。

SQLiteOpenHelper中还有两个非常重要的实例方法：getReadableDatabase()和getWritableDatabase()。这两个方法都可以创建或打开一个现有的数据库（如果数据库已存在则直接打开，否则要创建一个新的数据库），并返回一个可对数据库进行读写操作的对象。不同的是，当数据库不可写入的时候（如磁盘空间已满），getReadableDatabase()方法返回的对象将以只读的方式打开数据库，而getWritableDatabase()方法则将出现异常。SQLiteOpenHelper中有两个构造方法可供重写，一般使用参数少一点的那个构造方法即可。这个构造方法中接收4个参数：第一个参数Context；第二个参数是数据库名，创建数据库时使用的就是这里指定的名称；第三个参数允许我们在查询数据的时返回一个自定义的Cursor，一般传入null即可；第四个参数表示当前数据库的版本号，可用于对数据库进行升级操作。构建出SQLiteOpenHelper的实例之后，再调用它的getReadableDatabase()或getWritableDatabase()方法就能够创建数据库了，数据库文件会存放在/data/data/<package name>/databases/目录下。此时，重写的onCreate()方法也会得到执行，所以通常会在这里处理一些创建表的逻辑。

### 升级数据库
还记得SQLiteOpenHelper的构造方法里接收的第四个参数吗？它表示当前数据库的版本号，之前我们传入的是1，现在只要传入一个比1大的数，就可以让onUpgrade()方法得到执行了。

### 添加数据
SQLiteDatabase中提供了一个insert()方法，专门用于添加数据。它接收3个参数：第一个参数是表名，我们希望向哪张表里添加数据，这里就传入该表的名字；第二个参数用于在未指定添加数据的情况下给某些可为空的列自动赋值NULL，一般我们用不到这个功能，直接传入null即可；第三个参数是一个ContentValues对象，它提供了一系列的put()方法重载，用于向ContentValues中添加数据，只需要将表中的每个列名以及相应的待添加数据传入即可。

### 更新数据
调用了SQLiteDatabase的update()方法执行具体的更新操作，可以看到，这里使用了第三、第四个参数来指定具体更新哪几行。第三个参数对应的是SQL语句的where部分，表示更新所有name等于?的行，而?是一个占位符，可以通过第四个参数提供的一个字符串数组为第三个参数中的每个占位符指定相应的内容。

### 删除数据
SQLiteDatabase中提供了一个delete()方法，专门用于删除数据。这个方法接收3个参数：第一个参数仍然是表名，这个没什么好说的；第二、第三个参数用于约束删除某一行或某几行的数据，不指定的话默认会删除所有行。

### 查询数据
SQLiteDatabase中还提供了一个query()方法用于对数据进行查询。这个方法的参数非常复杂，最短的一个方法重载也需要传入7个参数。那我们就先来看一下这7个参数各自的含义吧。第一个参数不用说，当然还是表名，表示我们希望从哪张表中查询数据。第二个参数用于指定去查询哪几列，如果不指定则默认查询所有列。第三、第四个参数用于约束查询某一行或某几行的数据，不指定则默认查询所有行的数据。第五个参数用于指定需要去group by的列，不指定则表示不对查询结果进行group by操作。第六个参数用于对group by之后的数据进行进一步的过滤，不指定则表示不进行过滤。第七个参数用于指定查询结果的排序方式，不指定则表示使用默认的排序方式。

### 直接使用sql语句
除了查询数据的时候调用的是SQLiteDatabase的rawQuery()方法，其他操作都是调用的execSQL()方法。

### 使用事务
首先调用SQLiteDatabase的beginTransaction()方法开启一个事务，然后在一个异常捕获的代码块中执行具体的数据库操作，当所有的操作都完成之后，调用setTransactionSuccessful()表示事务已经执行成功了，最后在finally代码块中调用endTransaction()结束事务。

## JetPack  Room

### 增删改查

先来看一下Room的整体结构。它主要由Entity、Dao和Database这3部分组成，每个部分都有明确的职责，详细说明如下。
* Entity。用于定义封装实际数据的实体类，每个实体类都会在数据库中有一张对应的表，并且表中的列是根据实体类中的字段自动生成的。
* Dao。Dao是数据访问对象的意思，通常会在这里对数据库的各项操作进行封装，在实际编程的时候，逻辑层就不需要和底层数据库打交道了，直接和Dao层进行交互即可。
* Database。用于定义数据库中的关键信息，包括数据库的版本号、包含哪些实体类以及提供Dao层的访问实例。
```
@Entity
data class User(var firstName: String, var lastName: String, var age: Int) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
```
这里我们在User的类名上使用@Entity注解，将它声明成了一个实体类，然后在User类中添加了一个id字段，并使用@PrimaryKey注解将它设为了主键，再把autoGenerate参数指定成true，使得主键的值是自动生成的。
```
@Dao
interface UserDao {

    @Insert
    fun insertUser(user: User): Long

    @Update
    fun updateUser(newUser: User)

    @Query("select * from User")
    fun loadAllUsers(): List<User>

    @Query("select * from User where age > :age")
    fun loadUsersOlderThan(age: Int): List<User>

    @Delete
    fun deleteUser(user: User)

    @Query("delete from User where lastName = :lastName")
    fun deleteUserByLastName(lastName: String): Int

}
```
UserDao接口的上面使用了一个@Dao注解，这样Room才能将它识别成一个Dao。UserDao的内部就是根据业务需求对各种数据库操作进行的封装。数据库操作通常有增删改查这4种，因此Room也提供了@Insert、@Delete、@Update和@Query这4种相应的注解。
可以看到，insertUser()方法上面使用了@Insert注解，表示会将参数中传入的User对象插入数据库中，插入完成后还会将自动生成的主键id值返回。updateUser()方法上面使用了@Update注解，表示会将参数中传入的User对象更新到数据库当中。deleteUser()方法上面使用了@Delete注解，表示会将参数传入的User对象从数据库中删除。以上几种数据库操作都是直接使用注解标识即可，不用编写SQL语句。
但是如果想要从数据库中查询数据，或者使用非实体类参数来增删改数据，那么就必须编写SQL语句了。比如说我们在UserDao接口中定义了一个loadAllUsers()方法，用于从数据库中查询所有的用户，如果只使用一个@Query注解，Room将无法知道我们想要查询哪些数据，因此必须在@Query注解中编写具体的SQL语句才行。我们还可以将方法中传入的参数指定到SQL语句当中，比如loadUsersOlderThan()方法就可以查询所有年龄大于指定参数的用户。另外，如果是使用非实体类参数来增删改数据，那么也要编写SQL语句才行，而且这个时候不能使用@Insert、@Delete或@Update注解，而是都要使用@Query注解才行，参考deleteUserByLastName()方法的写法。

定义Database。这部分内容的写法是非常固定的，只需要定义好3个部分的内容：数据库的版本号、包含哪些实体类，以及提供Dao层的访问实例。新建一个AppDatabase.kt文件，代码如下所示：
```
@Database(version = 1, entities = [User::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    
    companion object {
        
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
                .build().apply {
                instance = this
            }
        }
    }
}
```
可以看到，这里我们在AppDatabase类的头部使用了@Database注解，并在注解中声明了数据库的版本号以及包含哪些实体类，多个实体类之间用逗号隔开即可。
另外，AppDatabase类必须继承自RoomDatabase类，并且一定要使用abstract关键字将它声明成抽象类，然后提供相应的抽象方法，用于获取之前编写的Dao的实例，比如这里提供的userDao()方法。不过我们只需要进行方法声明就可以了，具体的方法实现是由Room在底层自动完成的。
紧接着，我们在companion object结构体中编写了一个单例模式，因为原则上全局应该只存在一份AppDatabase的实例。这里使用了instance变量来缓存AppDatabase的实例，然后在getDatabase()方法中判断：如果instance变量不为空就直接返回，否则就调用Room.databaseBuilder()方法来构建一个AppDatabase的实例。databaseBuilder()
方法接收3个参数，注意第一个参数一定要使用applicationContext，而不能使用普通的context，否则容易出现内存泄漏的情况，第二个参数是AppDatabase的Class类型，第三个参数是数据库名，这些都比较简单。最后调用build()方法完成构建，并将创建出来的实例赋值给instance变量，然后返回当前实例即可。

另外，由于数据库操作属于耗时操作，Room默认是不允许在主线程中进行数据库操作的，因此增删改查的功能都要放到子线程中。不过为了方便测试，Room还提供了一个更加简单的方法，如下所示：
```
Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,"app_database")
 .allowMainThreadQueries()
 .build()
 ```
在构建AppDatabase实例的时候，加入一个allowMainThreadQueries()方法，这样Room就允许在主线程中进行数据库操作了，这个方法建议只在测试环境下使用。


### Room的数据库升级
如果你目前还只是在开发测试阶段，不想编写那么烦琐的数据库升级逻辑，Room倒也提供了一个简单粗暴的方法，如下所示：
```
Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,"app_database")
 .fallbackToDestructiveMigration()
 .build()
 ```
在构建AppDatabase实例的时候，加入一个fallbackToDestructiveMigration()方法。这样只要数据库进行了升级，Room就会将当前的数据库销毁，然后再重新创建，随之而来的副作用就是之前数据库中的所有数据就全部丢失了。
接下来我们还是老老实实学习一下在Room中升级数据库的正规写法。
随着业务逻辑的升级，现在我们打算在数据库中添加一张Book表，那么首先要做的就是创建一个Book的实体类，如下所示：
```
@Entity
data class Book(var name: String, var pages: Int) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
```
可以看到，Book类中包含了主键id、书名、页数这几个字段，并且我们还使用@Entity注解将
它声明成了一个实体类。
然后创建一个BookDao接口，并在其中随意定义一些API：
```
@Dao
interface BookDao {
    @Insert
    fun insertBook(book: Book): Long

    @Query("select * from Book")
    fun loadAllBooks(): List<Book>
}
```
接下来修改AppDatabase中的代码，在里面编写数据库升级的逻辑，如下所示：
```
@Database(version = 3, entities = [User::class, Book::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun bookDao(): BookDao

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("create table Book (id integer primary key autoincrement not null, name text not null, pages integer not null)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("alter table Book add column author text not null default 'unknown'")
            }
        }

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build().apply {
                    instance = this
                }
        }
    }
}
```
在companion object结构体中，我们实现了一个Migration的匿名类，并传入了1和 2这两个参数，表示当数据库版本从1升级到2的时候就执行这个匿名类中的升级逻辑。匿名类实例的变量命名也比较有讲究，这里命名成MIGRATION_1_2，可读性更高。由于我们要新增一张Book表，所以需要在migrate()方法中编写相应的建表语句。另外必须注意的是，Book表的建表语句必须和Book实体类中声明的结构完全一致，否则Room就会抛出异常。最后在构建AppDatabase实例的时候，加入一个addMigrations()方法，并把MIGRATION_1_2传入即可。