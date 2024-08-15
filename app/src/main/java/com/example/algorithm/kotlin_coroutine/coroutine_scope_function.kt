//package com.example.algorithm.kotlin_coroutine
//
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.TimeoutCancellationException
//import kotlinx.coroutines.async
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.supervisorScope
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.withTimeout
//import kotlinx.coroutines.withTimeoutOrNull
//import kotlinx.coroutines.yield
//import kotlin.coroutines.CoroutineContext
//import kotlin.coroutines.EmptyCoroutineContext
//
///* 11장 코루틴 스코프 함수 */
//
//
///* 코루틴 스코프함수가 사용되기전에 사용한 방법들 */
//
///* 1.중단함수에서 중단함수를 호출하는 방법 */
//// 가장 큰 문제!! => 작업이 동시에 진행되지 않음(순차적으로 진행되는 문제)
//suspend fun getUserProfile1(): UserProfileData {
//    val user = getUserData() // (1초 후)
//    val notifications = getNotifications() // (1초 후)
//
//    return UserProfileData(
//        user = user,
//        notifications = notifications,
//    )
//}
//// 1초가 걸릴게 동시에 실행이 안되기때문에 (1초+1초) = 2초가 걸림
//
//
//
//// 이렇게 구현하면 안 됩니다!
///* 2.중단함수에서 중단함수를 호출하는 방법 - async사용 */
//// 위 함수를 동시에 진행하려면 async로 래핑하면됨, 근데 GlobalScope빌더를 사용하는건 좋은방법이 아님
//suspend fun getUserProfile2(): UserProfileData {
//    val user = GlobalScope.async { getUserData() }
//    val notifications = GlobalScope.async {
//        getNotifications()
//    }
//    return UserProfileData(
//        user = user.await(), // (1초 후)
//        notifications = notifications.await(),
//    )
//}
//// 두 중단함수가 동시에 진행되므로 1초만 걸림
//
//
//
//// GlobalScope 내부코드
//// EmptyCoroutineContext를 가진 스코프일뿐
//public object GlobalScope : CoroutineScope{
//    override val coroutineContext : CoroutineContext
//        get() = EmptyCoroutineContext
//}
//
//
//
//
//// 이렇게 구현하면 안 됩니다!
//// 함수의 호출자에게 CoroutineScope를 제공함 -> 호출자가 적절한 스코프를 선택하지 못할경우 예상치못한 부작용이 발생할 수 있음
//suspend fun getUserProfile3(
//    scope: CoroutineScope
//): UserProfileData {
//    val user = scope.async { getUserData() }
//    val notifications = scope.async { getNotifications () }
//
//    return UserProfileData (
//        user = user.await(), // (1초 후)
//        notifications = notifications.await(),
//    )
//}
//// 또는 이렇게 구현하면 안 됩니다!
//// getUserProfile의 확장함수
//suspend fun CoroutineScope.getUserProfile(): UserProfileData {
//    // async빌더를 스코프내에서 사용하는게 아니라, 함수내에서 사용하는것은 위험함
//    // 해당 스코프가 취소되면 anync빌더에 있는 모든 코루틴이 같이 취소되기떄문에, 메모리누수발생+코루틴이 중단될수있음
//    val user = async { getUserData() }
//    val notifications = async { getNotifications() }
//
//    return UserProfileData(
//        user = user.await(), // (1초 후)
//        notifications = notifications.await(),
//    )
//}
//
//
//
//
//
//data class Details(val name: String, val followers: Int)
//data class Tweet(val text: String)
//
//fun getFollowersNumber(): Int =
//    throw Error("Service exception")
//
//suspend fun getUserName(): String {
//    delay(500)
//    return "marcinmoskala"
//}
//
//suspend fun getTweets(): List<Tweet> {
//    return listOf (Tweet ("Hello, world"))
//}
//
//suspend fun CoroutineScope.getUserDetails(): Details {
//        val userName = async { getUserName() }
//        val followersNumber = async { getFollowersNumber() }    // 예외 던짐 -> catch문으로 감
//        return Details (userName.await(), followersNumber.await())
//    }
//
//fun mains1() = runBlocking {
//    val details = try {
//        getUserDetails()
//    } catch (e: Error) {
//        null
//    }
//
//    val tweets = async { getTweets() }
//    println("User: $details")
//    println("Tweets: ${tweets.await()}")
//}
//// 예외만 발생합니다.
//
//
//
//
//
//
///* coroutineScope */
//// coroutineScope => 스코프를 시작하는 중단함수
//// 인자로 들어온 함수가 생성한 값을 반환(여기선 R값 리턴)
//suspend fun <R> coroutineScope(
//    block: suspend CoroutineScope.() -> R
//): R
//
//
//
//
//fun main() = runBlocking {      // 메인스레드를 블로킹(코루틴이 완료될때까지 기다림)
//    val a = coroutineScope {    // 새로운 코루틴 시작 / 블록내에서 모든작업이 완료될때까지 기다림
//        delay(1000)
//        10                      // coroutineScope블록의 결과로 반환
//    }
//    println("a is calculated")
//    val b = coroutineScope {
//        delay(1000)
//        20
//    }
//    println(a) // 10
//    println(b) // 20
//}
//// 1초후
//// a is calculated
//// 1초후
//// 10
//// 20
//
//
//// coroutineScope는 모든 자식이 완료될때까지 기다림!
//// longTask1은 coroutineScope로 감싸져있기때문에 coroutineScope내의 자식코루틴이 모두 완료된후에야 After출력!!
//suspend fun longTask1() = coroutineScope {  //새로운 코루틴시작 //모든자식이 완료될때까지 기다림
//    launch {
//        delay(1000)
//        val name = coroutineContext[CoroutineName]?.name
//        println("[$name] Finished task 1")
//    }
//    launch {
//        delay(2000)
//        val name = coroutineContext[CoroutineName]?.name
//        println("[$name] Finished task 2")
//    }
//}
//
//fun main_02() = runBlocking(CoroutineName("Parent")) {
//    println("Before")
//    longTask1()
//    println("After")
//}
//// Before
//// (1초 후)
//// [Parent] Finished task 1
//// (1초 후)
//// [Parent] Finished task 2
//// After
//
//
//
////coroutineScope은 부모가 취소되면 아직 끝나지않은 자식코루틴도 전부취소됨
//suspend fun longTask2() = coroutineScope {
//    launch {
//        delay(1000)        // 1초중단
//        val name = coroutineContext[CoroutineName]?.name
//        println("[$name] Finished task 1")
//    }
//    launch {
//        delay(2000)     // 2초중단 (1.5초뒤에 취소되서 이블록은 실행X)
//        val name = coroutineContext[CoroutineName]?.name
//        println("[$name] Finished task 2")
//    }
//}
//
//    fun mains_1(): Unit = runBlocking {
//        val job = launch(CoroutineName("Parent")) {
//            longTask2()
//        }
//        delay(1500)
//        job.cancel()    // 1.5초후에 취소
//    }
//
//// [Parent] Finished task 1
//
//
//
//
//
//data class Details1(val name: String, val followers: Int)
//data class Tweet1(val text: String)
//class ApiException(
//    val code: Int,
//    message: String
//) : Throwable (message)
//
//fun getFollowersNumber1(): Int =
//    throw ApiException(500, "Service unavailable")  //예외발생
//
//suspend fun getUserName1(): String {
//    delay(500)
//    return "marcinmoskala"
//}
//
//suspend fun getTweets1(): List<Tweet1> {
//    return listOf(Tweet1("Hello, world"))
//}
//
//    suspend fun getUserDetails(): Details1 = coroutineScope {
//        // 바동기작업을 병렬적으로 수행
//        val userName = async { getUserName1() }
//        val followersNumber = async { getFollowersNumber1() }
//        Details1(userName.await(), followersNumber.await()) // await()을 호출해 두코루틴이 완료될때까지 기다리고, Details1객체에 반환
//    }
//
//        fun mains_2() = runBlocking<Unit> {
//            val details = try {
//                getUserDetails()
//            } catch (e: ApiException) { //getFollowersNumber1()에서 예외발생하므로, details에 null할당
//                null
//            }
//            val tweets = async { getTweets1() }
//            println("User: $details")
//            println("Tweets: ${tweets.await()}")
//        }
//// User: null
//// Tweets: [Tweet(text=Hello, world)]
//
//
//
//
//
//// 앞에서 작성한 예제코드를 coroutineScope를 사용해서 수정
//// 중단함수에서 병렬로 작업을 수행할경우, coroutineScope를 사용하는것이 좋음
//suspend fun getUserProfile4(): UserProfileData =
//    coroutineScope {    //새로운 코루틴시작 //모든자식이 완료될때까지 기다림
//        // 두 비동기작업은 동시에 실행
//        val user = async { getUserData() }
//        val notifications = async{ getNotifications() }
//
//        UserProfileData(
//            // 각각의 비동기작업이 모두 완료된후에 결과 반환
//            user = user.await(),
//            notifications = notifications.await(),
//        )
//    }
//
//
//
//// coroutineScope는 중단메인함수 본체를 래핑할때 주로 사용
//suspend fun mains_3() : Unit = coroutineScope {
//    launch {
//        delay(1000)
//        println("World")
//    }
//    print("Hello,")
//}
//// Hello
//// (1초후)
//// World
//
//
//// produceCurrentUserSeq함수는 getProfile과 getFriends을 연속으로 호출하고
//// produceCurrentUserSym함수는 함수는 병렬로 호출하는것을 제외하면 두함수는 별차이가 없음
//suspend fun produceCurrentUserSeq(): User {
//    val profile = repo.getProfile()
//    val friends = repo.getFriends()
//    return User(profile, friends)
//}
//suspend fun produceCurrentUserSym(): User = coroutineScope {
//    val profile = async { repo.getProfile() }
//    val friends = async { repo.getFriends() }
//    User(profile.await(), friends.await())
//}
//
//
//
//// withContext(EmptyCoroutineContext)와 CoroutineScope은 완벽히 같은 방식으로 동작!
//fun CoroutineScope.log(text: String) {
//    val name = this.coroutineContext[CoroutineName]?.name
//    println("[$name] $text")
//}
//fun mains_4() = runBlocking(CoroutineName("Parent")) {
//    log("Before")
//
//    withContext(CoroutineName("Child 1")) {
//        delay(1000)
//        log("Hello 1")
//    }
//
//    withContext(CoroutineName("Child 2")) {
//        delay(1000)
//        log("Hello 2")
//    }
//    log("After")
//}
//// [Parent] Before
////  1초후
//// [Child 1] Hello 1
////  1초후
//// [Child 2] Hello 2
//// [Parent] After
//
//
//
//// withContext함수는 기존스코프와 컨텍스트가 다른 코루틴스코프를 설정하기위해 주로사용(Dispatchers에서 주로사용)
//launch(Dispatchers.Main) {
//    view.showProgressBar()
//    withContext(Dispatchers.IO) {
//        fileRepository.saveData(data)
//    }
//    view.hideProgressBar()
//}
//
//
//
//// supervisorScope은 SupervisorJob을 오버라이딩하기때문에, 예외를 던지더라도 독립적으로만 취소됨
//fun mains5() = runBlocking{     // 최상위부모
//    println("Before")
//
//    supervisorScope{    //부모
//        launch {        //자식1
//            delay(1000)
//            throw Error()   // supervisorScope이기 때문에 자식1만 취소
//        }
//        launch {        //자식2   //취소X
//            delay(2000)
//            println("Done")
//        }
//    }
//    println("After")
//// Before
//// (1초 후)
//// 예외가 발생합니다...
//// (1초 후)
//// Done
//// After
//
//
//
//// supervisorScope를 사용해, 개별액션처리중에 발생하는 예외가 다른 액션처리에 영향을 주지 않도록하는 코드
//// 병령처리가 필요하면서도, 서로 독립적인 작업을 시작해야하는 상황에서 주로 사용
//suspend fun notifyAnalytics(actions: List<UserAction>) =
//    supervisorScope{
//        actions.forEach { action ->
//            launch {
//                notifyAnalytics(action)
//            }
//    }
//}
//
//
//
//// ArticleRepository에서 비동기적으로 데이터를 가져와, 하나의 리스트로 병합한 후, 내림차순으로 정렬된 결과를 반환하는 클래스
//// supervisorScope을 사용해 비동기작업을 독립적으로처리하고, 예외발생해도 다른 작업에 영향을 안주도록 함
//class ArticlesRepositoryComposite(
//    private val articleRepositories: List<ArticleRepository>,
//) : ArticleRepository {
//    override suspend fun fetchArticles(): List<Article> =
//        supervisorScope {
//            articleRepositories
//                .map { async { it.fetchArticles() } }   //fetchArticles함수가 비동기적으로 호출
//                .mapNotNull {
//                    try {
//                        it.await()  //Deferred의 결과반환
//                    } catch (e: Throwable) {    //예외발생시
//                        e.printStackTrace()
//                        null
//                    }
//                }
//                .flatten()  //ArticleRepository가 반환한 기사를 하나의 리스트로 병합하는 역할
//                .sortedByDescending { it.publishedAt }  //내림차순으로 정렬
//        }
//}
//
//
//
//
//// supervisorScope대신 withContext(SupervisorJob())을 사용할 수 X
//// 자식코루틴이 예외를 던지면 다른 자식들또한 취소됨 / withContext도 예외를 던지기 때문에 사실상 SupervisorJob()은 쓸모가없게됨
//// 결국 withContext(SupervisorJob())을 사용해도 의미가 X
//fun main_5() = runBlocking {
//    println("Before")
//
//    withContext(SupervisorJob()) {  //부모
//        launch {    //자식1
//            delay(1000)
//            throw Error()
//        }
//        launch {    //자식2
//            delay(2000)
//            println("Done")
//        }
//    }
//    println("After")
//}
//// Before
//// (1초 후)
//// Exception...
//
//
//
//// withTimeout => 일정시간내에 코루틴이 완료되지 않으면, TimeoutCancellationException을 던지며 코루틴취소
//suspend fun test(): Int = withTimeout(1500) {   //1.5초동안 완료되지않으면 코루틴 취소
//    delay ( 1000)
//    println("Still thinking")
//    delay ( 1000)
//    println ("Done!")
//    42
//}
//
//suspend fun mains_006(): Unit = coroutineScope{
//        try{
//            test ()
//        } catch (e: TimeoutCancellationException) {
//            println ("Cancelled" )
//        }
//        delay(1000) //test 함수가 취소되었기 때문에,타임아웃 시간을 늘려도 아무런 도움이 되지 않음
//}
//// (1초 후)
//// Still thinking
//// (0.5초 후)
//// Cancelled
//
//
//
//
//// runTest내부에서 withTimeout을 사용하면, withTimeout은 가상시간으로 사용됨
//class Test {
//    @Test
//    fun testTime2() = runTest {
//        withTimeout(1000) {
//            // 1000ms보다 적게 걸리는 작업
//            delay(900) // 가상 시간
//        }
//    }
//
//    @Test(expected = TimeoutCancellationException::class)
//    fun tesTime1() = runTest {
//        withTimeout(1000) {
//            // 1000ms보다 오래 걸리는 작업
//            delay(1100) // 가상 시간
//        }
//    }
//
//    // runBlocking내부에서도 withTimeout사용 가능
//    // 실제시간 테스트
//    @Test
//    fun testTine3() = runBlocking {
//        withTimeout(1000) {
//            // 그다지 오래 걸리지 않는 일반적인 테스트
//            delay(900) // 실제로 900ms만큼 기다립니다.(실제시간)
//        }
//    }
//}
//
//
//
//// 코루틴 빌더 내부에서 TimeoutCancellationException을 던지면 해당코루틴만 취소가되고, 나머지자식들에게는 영향X (독립적으로 취소)
//suspend fun main_6(): Unit = coroutineScope{
//    launch {        // 1
//        launch {    // 2, 부모에 의해 취소됩니다.
//            delay (2000)
//            println("Will not be printed")
//        }
//        withTimeout(1000) {     // 이 코루틴이 launch를 취소합니다.
//            delay (1500)
//        }
//    }
//    launch { // 3       //취소X
//        delay (2000)
//        println ("Done")
//    }
//}
//// (2초 후)
//// Done
//
//
//
//
//// withTimeoutOrNull => 타임아웃을 초과하면 예외를 던지지않고, 람다식이 취소되고 null이 반환됨
//// 함수에서 걸리는 시간이 너무 길때, 잘못되었음을 알리는 용도로 사용함
//suspend fun fetchUser(): User {
//    // 영원히 실행됩니다.
//    while (true) {
//        yield()
//    }
//}
//// fetchUser()가 5초동안 데이터 반환하지 않기때문에, null을 반환함
//suspend fun getUserOrNull(): User? =
//    withTimeoutOrNull(5000) {   //5초동안 fetchUser()함수 기다림
//        fetchUser()
//    }
//
//suspend fun mains_007(): Unit = coroutineScope {
//    val user = getUserOrNull()  //getUserOrNull()은 항상 null을 반환함
//    println("User: $user")
//}
//// 5초후
//// User: null
//
//
//
//// 작업을 수행하는도중 추가적인 연산을 수행하는 경우
//// 이 방식의 문제 -> coroutineScope가 사용자 데이터를 보여준뒤, launch코루틴이 끝나기를 기다려야함 -> 효율적이지 않음
//// 뷰를 업데이트할때 프로그레스바를 보여주고있다면, notifyProfileShown이 끝날때까지 기다려야함
//class ShowUserDataUseCase(
//    private val repo: UserDataRepository,
//    private val view: UserDataView,
//) {
//    // 비동기적으로 사용자 데이터를 가져오고, 뷰에 표시한후 저장소에 프로필이 표시되었음을 알림
//    suspend fun showUserData() = coroutineScope {   //모든 코루틴이 완료될때까지 기다림
//        val name = async { repo.getName() }
//        val friends = async { repo.getFriends() }
//        val profile = async { repo.getProfile() }
//        val user = User(
//            name = name.await(),
//            friends = friends.await(),
//            profile = profile.await(),
//        )
//        view.show(user)
//        launch { repo.notifyProfileShown() }    // 저장소에 프로필이 화면에 표시되었음을 알림(끝날떄까지 기다린후 실행되기때문에 시간오래걸림)
//    }
//}
//
//
//
//
//// CoroutineScope(SupervisorJob()) -> 생성자를 통해 SupervisorJob()을 주입해서, 추가적인 연산을 위한 스코프를 만듦
//// 이를통해 끝날때까지 기다리지 않고, 만약 notifyProfileShown()에서 예외가 발생하더라도 얘때문에 다른자식이 취소되지X
//class ShowserDataUseCase(
//        private val repo: UserDataRepository,
//        private val view: UserDataView,
//        private val analyticscope: CoroutineScope,
//){
//    suspend fun showUserData() = coroutineScope {
//        val name = async { repo.getName()}
//        val friends = async { repo.getFriends() }
//        val profile = async { repo.getProfile() }
//        val user = User (
//            name = name.await(),
//            friends = friends.await(),
//            profile = profile.await()
//        )
//        view.show(user)
//        val analyticsScope = CoroutineScope(SupervisorJob())    //생성자를 통해 SupervisorJob()을 주입해서, 추가적인 연산을 위한 스코프를 만듦 => 끝날때까지 기다리지X
//        analyticsScope.launch { repo.notifyProfileShown() }
//    }
//}
//
