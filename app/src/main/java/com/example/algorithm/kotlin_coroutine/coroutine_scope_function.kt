package com.example.algorithm.kotlin_coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/* 11장 코루틴 스코프 함수 */

// 데이터를 동시에 가져오지 않고, 순차적으로 가지옵니다.
suspend fun getUserProfile1(): UserProfileData {
    val user = getUserData() // (1초 후)
    val notifications = getNotifications() // (1초 후)

    return UserProfileData(
        user = user,
        notifications = notifications,
    )
}


// 이렇게 구현하면 안 됩니다!
suspend fun getUserProfile2(): UserProfileData {
    val user = GlobalScope.async { getUserData() }
    val notifications = GlobalScope.async {
        getNotifications()
    }
    return UserProfileData(
        user = user.await(), // (1초 후)
        notifications = notifications.await(),
    )
}


public object GlobalScope : CoroutineScope{
    override val coroutineContext : CoroutineContext
        get() = EmptyCoroutineContext
}


// 이렇게 구현하면 안 됩니다!
suspend fun getUserProfile3(
    scope: CoroutineScope
): UserProfileData {
    val user = scope.async { getUserData() }
    val notifications = scope.async { getNotifications () }

    return UserProfileData (
        user = user.await(), // (1초 후)
        notifications = notifications.await(),
    )
}
// 또는
// 이렇게 구현하면 안 됩니다!
suspend fun CoroutineScope.getUserProfile(): UserProfileData {
    val user = async { getUserData() }
    val notifications = async { getNotifications() }

    return UserProfileData(
        user = user.await(), // (1초 후)
        notifications = notifications.await(),
    )
}




data class Details(val name: String, val followers: Int)
data class Tweet(val text: String)

fun getFollowersNumber(): Int =
    throw Error("Service exception")

suspend fun getUserName(): String {
    delay(500)
    return "marcinmoskala"
}

suspend fun getTweets(): List<Tweet> {
    return listOf (Tweet ("Hello, world"'))
}

suspend fun CoroutineScope.getUserDetails(): Details {
        val userName = async { getUserName() }
        val followersNumber = async { getFollowersNumber() }
        return Details (userName.await(), followersNumber.await())
    }

fun main() = runBlocking {
    val details = try {
        getUserDetails()
    } catch (e: Error) {
        null
    }

    val tweets = async { getTweets () }
    println("User: $details")
    println("Tweets: ${tweets.await()}")
}
// 예외만 발생합니다.


suspend fun <R> coroutineScope(
    block: suspend CoroutineScope.() -> R
): R



fun main() = runBlocking {
    val a = coroutineScope {
        delay(1000)
        10
    }
    println("a is calculated")
    val b = coroutineScope {
        delay(1000)
        20
    }
    println(a) // 10
    println(b) // 20
}
// 1초후
// a is calculated
// 1초후
// 10
// 20



suspend fun longTask1() = coroutineScope {
    launch {
        delay(1000)
        val name = coroutineContext[CoroutineName]?.name
        println("[$name] Finished task 1")
    }
    launch {}
    delay(2000)
    val name = coroutineContext[CoroutineName]?.name
    println("[$name] Finished task 2")
    }
}

fun main_02() = runBlocking (CoroutineName("Parent")) {
    println("Before")
    longTask1()
    println("After")
}
// Before
// (1초 후)
// [Parent] Finished task 1
// (1초 후)
// [Parent] Finished task 2
// After



suspend fun longTask2() = coroutineScope {
    launch {
        delay(1080)
        val name = coroutineContext[CoroutineName]?.name
        println("[$name] Finished task 1")
    }
        launch {
            delay(2000)
            val name = coroutineContext[CoroutineName]?.name
            println("[$name] Finished task 2")
        }
    }

    fun mains_1(): Unit = runBlocking {
        val job = launch(CoroutineName("Parent")) {
            longTask2()
        }
        delay(1500)
        job.cancel()
    }

// [Parent] Finished task 1




data class Details(val name: String, val followers: Int)
data class Tweet(val text: String)
class ApiException(
    val code: Int,
    message: String
) : Throwable (message)

fun getFollowersNumber(): Int =
    throw ApiException(500, "Service unavailable")

suspend fun getUserName(): String (
    delay (500)
    return "marcinmoskala"
}

suspend fun getTweets(): List<Tweet> {
    return listOf(Tweet("Hello, world"))
}
    suspend fun getUserDetails(): Details = coroutineScope {
        val userName = async { getUserName() }
        val followersNumber = async { getFollowersNumber() }
        Details(userName.await(), followersNumber.await())
    }

        fun mains_2() = runBlocking<Unit> {
            val details = try {
                getUserDetails()
            } catch (e: ApiException) {
                null
            }
            val tweets = async { getTweets() }
            println("User: $details")
            println("Tweets: S{tweets.await()}")
        }
// User: null
// Tweets: [Tweet(text=Hello, world)]


suspend fun getUserProfile4(): UserProfileData =
    coroutineScope {
        val user = async { getUserData() }
        val notifications = async{ getNotifications() }

        UserProfileData(
            user = user.await(),
            notifications = notifications.await(),
        )
    }


suspend fun main_3() : Unit = coroutineScope {
    launch {
        delay(1000)
        println("World")
    }
    print("Hello,")
}
// Hello
// (1초후)
// World


suspend fun produceCurrentUserSeq(): User {
    val profile = repo.getProfile()
    val friends = repo.getFriends()
    return User(profile, friends)
}
suspend fun produceCurrentUserSym(): User = coroutineScope {
    val profile = async { repo.getProfile() }
    val friends = async { repo.getFriends() }
    User(profile.await(), friends.await())
}


fun CoroutineScope.log(text: String) {
    val name = this.coroutineContext[CoroutineName]?.name
    println("[$name] $text")
}
fun main_4() = runBlocking(CoroutineName("Parent")) {
    log("Before")

    withContext(CoroutineName("Child 1")) {
        delay(1000)
        log("Hello 1")
    }
    withContext(CoroutineName("Child 2")) {
        delay(1000)
        log("Hello 2")
    }
    log ("After")
}
// [Parent] Before
//  1초후
// [Child 1] Hello 1
//  1초후
// [Child 2] Hello 2
// [Parent] After



launch(Dispatchers.Main) {
    view.showProgressBar()
    withContext(Dispatchers.IO) {
        fileRepository.saveData(data)
    }
    view.hideProgressBar()
}



fun main() = runBlocking{
    println("Before")

    supervisorScope{
        launch (
            delay(1000)
            throw Error()
        }
        launch {
            delay(2080)
            println("Done")
        }
    }
    println("After")
}
// Before
// (1초 후)
// 예외가 발생합니다...
// (1초 후)
// Done
// After



suspend fun notifyAnalytics(actions: List<UserAction>) =
    supervisorScope{
        actions.forEach { action ->
            launch {
                notifyAnalytics(action)
            }
    }
}



class ArticlesRepositoryComposite(
    private val articleRepositories: List<ArticleRepositorys>,
) : ArticleRepository {
    override suspend fun fetchArticles(): List<Article> =
        supervisorScope {
            articleRepositories
                .map { async { it.fetchArticles() } }
                .mapNotNull {
                    try {
                        it.avait()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        null
                    }
                }
                .flatten()
                .sortedByDescending { it.publishedAt }
        }
}



fun main_5() = runBlocking {
    println("Before")

    withContext(SupervisorJob()) {
        launch {
            delay(1000)
            throw Error()
        }
        launch {
            delay(2000)
            println("Done")
        }
    }
    println("After")
}
// Before
// (1초 후)
// Exception...



suspend fun test(): Int = withTimeout(1500) {
    delay ( 1000)
    println("Still thinking")
    delay ( 1000)
    println ("Done!")
    42
}

suspend fun main(): Unit = coroutineScope{
        try{
            test ()
        } catch (e: TimeoutCancellationException) {
            println ("Cancelled" )
        }
        delay(1000) //test' 함수가 취소되었기 때문에, // 타임아웃 시간을 늘려도 아무런 도움이 되지 않습니다.

}
// (1초 후)
// Still thinking
// (0.5초 후)
// Cancelled


class Test {
    @Test
    fun testTime2() = runTest {
        withTimeout(1000) {
            // 1000ms보다 적게 걸리는 작업
            delay(900) // 가상 시간
        }
    }

    @Test(expected = TimeoutCancellationException::class)
    fun tesTime1() = runTest {
        withTimeout(1000) {
            // 1800ms보다 오래 걸리는 작업
            delay(1100) // 가상 시간
        }
    }

    @Test
    fun testTine3() = runBlocking {
        withTimeout(1000) {
            // 그다지 오래 걸리지 않는 일반적인 테스트
            delay(900) // 실제로 900ms만큼 기다립니다.
        }
    }
}


suspend fun main_6(): Unit = coroutineScope{
    launch {       // 1
        launch { // 2, 부모에 의해 취소됩니다.
            delay (2000)
            println("Will not be printed")
        }
        withTimeout(1000) { // 이 코루틴이 Launch를 취소합니다.
            delay (1500)
        }
    }
    launch { // 3
        delay (2000)
        println ("Done")
    }
}
// (2초 후)
// Done



suspend fun fetchUser(): User {
    // 영원히 살행됩니다.
    while (true) (
            yield()
    }
}

suspend fun getUserOrNull(): User? =
    withTimeoutOrNull (5000) {
        fetchUser()
    }

suspend fun main(): Unit = coroutineScope {
    val user = getUserOrNull()
    println("User: $user")
}
// 5초후
// User: null


suspend fun calculateAnswerOrNull(): User? =
    withContext(Dispatchers.Default) {
        withTimeoutOrNull(1000) {
            calculateAnswer()
        }
    }


class ShowUserDataUseCase(
    private val repo: UserDataRepository,
    private val view: UserDatavsew,
) {
    suspend fun shoUserData() = coroutineScope {
        val name = async { repo.getName() }
        val friends = async { repo.getFriends() }
        val profile = async { repo.getProfile() }
        val user = User(
            name = name.await(),
            friends = friends.await(),
            profile = profile.await()
        )
        view.show(user)
        launch { repo.notifyProfileShown() }
    }
}


fun oncreate() {
    viewModelScope.launch {
        _progressBar.value = true
        showserData()
        _progressBar.value = false
    }
}


class ShowserDataUseCase(
        private val repo: UserDataRepository,
        private val view: UserDataView,
        pravate val analyticscope: Coroutinescope,
){
    suspend fun showserData() = coroutineScope {
    val name = async { repo.getName()}
        val friends = async { repo.getFriends) }
        val profile = async { repo.getProfile() }
        val user = User (
            name = name.await(),
            friends = friends.await(),
            profile = profile.await()
        )
        view, show(user)
        analyticsScope.launch { repo.notifyProfileShown() }
    }
}

