package com.test.testandroidproject

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import com.test.testandroidproject.adapters.CustomAdapter
import com.test.testandroidproject.data.PostInfo
import com.test.testandroidproject.data.UserInfo
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var progress: ProgressBar
    private lateinit var textView: TextView
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: CustomAdapter
    private lateinit var titleList: List<UserInfo>
    private lateinit var expandList: LinkedHashMap<UserInfo, List<PostInfo>>
    private lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация view
        btnStart = findViewById(R.id.btn_start)
        textView = findViewById(R.id.textView)
        expandableListView = findViewById(R.id.list_users)

        // Настройка ExpandableListView чтобы не было делений между items
        expandableListView.setGroupIndicator(null)
        expandableListView.setChildIndicator(null)
        expandableListView.setChildDivider(ContextCompat.getDrawable(this,
            R.drawable.divider_child))

        progress = findViewById(R.id.progress)
        progress.visibility = View.GONE

        adjustActionBar()

        expandList = LinkedHashMap()
        titleList = ArrayList()
        adapter = CustomAdapter(this, titleList, expandList)
        expandableListView.setAdapter(adapter)
        hideList()
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        /* Во время нажатия на кнопку, отображается progress, скрываются кнопка и текст,
        проверяется наличие интернета. Если интернет отсутствует, отображается соответствующее сообщение,
        если интернет есть -> делается 2 запроса паралельно с помощью корутин,
        потом результаты запросов ждут друг друга и когда все запросы завершились и результаты пришли,
        данные передаются в адаптер списка и обновляется адаптер */

        btnStart.setOnClickListener {
            showProgress()
            hideButtonAndText()
            if (checkInternet(this)) {

                // Запускается корутин в котором делаются запросы на сервер
                GlobalScope.launch(Dispatchers.IO) {

                    // Результат первого запроса (сам метод запроса)
                    val userInfoList = async { getUsers() }

                    // Результат второго запроса (сам метод запроса)
                    val postInfoList = async { getPosts() }

                    /* Принимаются данные и передаются в адаптер
                     userInfoList.await() и postInfoList.await() <- с помощью метода await()
                     каждый корутин ждет друг друг и когда все выполнили свою работу,
                     передают данные дальше */

                    fillData(userInfoList.await(), postInfoList.await())
                }
            } else {

                // Небольшой таймер для отображения progress
                val timer = object : CountDownTimer(1500, 1) {
                    override fun onTick(p0: Long) {
                    }

                    override fun onFinish() {

                        // Функции для отображения информации об отсутствии интернета
                        showButtonAndText()
                        hideProgress()
                        oops()
                    }
                }
                timer.start()
            }
        }

        // Метод для обработки нажатия на позицию списка
        expandableListView.setOnGroupExpandListener { groupPosition ->
            // handle click
        }

        // Метод для обработки нажатия на позицию подсписка
        expandableListView.setOnChildClickListener { parent, view, groupPosition, childPosition, id ->
            // handle click
            false
        }

    }

    // Запрос на сервер для получения инфо о пользователях
    private suspend fun getUsers(): List<UserInfo>? = suspendCoroutine { cont ->

        // Подготовка сервиса Retrofit
        val service = retrofit.create(RetrofService::class.java)

        // Подготовка вызова Retrofit
        val call = service.getUsers()

        // Делается запрос на сервер List<UserInfo> - это тип данных который возвратится в onResponse
        call.enqueue(object : Callback<List<UserInfo>> {
            override fun onResponse(
                call: Call<List<UserInfo>>?,
                response: Response<List<UserInfo>>?,
            ) {

                // Проверяем на успешное выполнение
                if (response?.code() == 200) {

                    /* cont - это объект для управления корутин,
                    в него передаем наш результат (вот такой List<UserInfo>)
                    а метод resume возобновляет работу метода */

                    cont.resume(response.body())
                }
            }

            override fun onFailure(call: Call<List<UserInfo>>?, t: Throwable?) {

                // Здесь в метод resume передаем null, потому что произошла ошибка и у нас нет наших данных
                cont.resume(null)
            }
        })
    }

    // Запрос на сервер для получения постов о пользователях, описание метода аналогично первому
    private suspend fun getPosts(): List<PostInfo>? = suspendCoroutine { cont ->
        val service = retrofit.create(RetrofService::class.java)
        val call = service.getPosts()
        call.enqueue(object : Callback<List<PostInfo>> {
            override fun onResponse(
                call: Call<List<PostInfo>>?,
                response: Response<List<PostInfo>>?,
            ) {
                if (response?.code() == 200) {
                    cont.resume(response.body())
                }
            }

            override fun onFailure(call: Call<List<PostInfo>>?, t: Throwable?) {
                Log.i("Key", "getPosts > onFailure > " + t.toString())
                cont.resume(null)
            }
        })
    }

    /* Метод принимает данные с обеих запросов
     List<UserInfo> - список юзеров
     List<PostInfo> - список постов
     каждый юзер имеет по несколько постов,
     нам надо будет отделить каждому юзеру его посты по id (JSON каждого запроса) */

    private fun fillData(userDataList: List<UserInfo>?, postDataList: List<PostInfo>?) {
        val hashMap = LinkedHashMap<UserInfo, List<PostInfo>>()
        if (userDataList != null) {

            // Пробежимся по всем пользователям в цикле
            for (userInfo in userDataList) {

                /* Для каждого пользователя
                 метод "postDataList!!.partition { (it.userId == userInfo.id) }" берет список всех постов,
                 в условии вот здесь -> (it.userId == userInfo.id) проверяется соответствие id пользователя и его поста,
                 и все посты этого пользователя превращаются в отделный список listOfPosts */

                val (listOfPosts, second) = postDataList!!.partition { (it.userId == userInfo.id) }

                // Наполняем наш список уже подготовленными данными
                hashMap.put(userInfo, listOfPosts)
            }
        }

        // Передаем подготовленные данные в адаптер списка
        val userInfo = ArrayList(hashMap.keys)
        adapter.setData(userInfo, hashMap)
        runOnUiThread {

            // Обновляем список, прячем ненужные view
            adapter.notifyDataSetChanged()
            hideProgress()
            showList()
        }
    }

    // Скрыть кнопку и текст
    private fun hideButtonAndText() {
        btnStart.visibility = View.GONE
        textView.visibility = View.GONE
    }

    // Показать кнопку и текст
    private fun showButtonAndText() {
        btnStart.visibility = View.VISIBLE
        textView.visibility = View.VISIBLE
    }

    // Показать список
    private fun showList() {
        expandableListView.visibility = View.VISIBLE
    }

    // Скрыть список
    private fun hideList() {
        expandableListView.visibility = View.GONE
    }

    // Показать progressBar
    private fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    // Скрыть progressBar
    private fun hideProgress() {
        progress.visibility = View.GONE
    }

    // Метод для скрытия списка и присваивания текста в случае отсутствия интернета
    private fun oops() {
        textView.text = resources.getString(R.string.ooops)
        btnStart.text = resources.getString(R.string.refresh)
        hideList()
    }

    // Присваивается дефолтный текст кнопке и тексту
    private fun setTextToButton() {
        textView.text = resources.getString(R.string.upload_users_and_their_posts)
        btnStart.text = resources.getString(R.string.start)
    }

    // Метод для проверки наличия интернета
    private fun checkInternet(context: Context): Boolean {
        var isConnected = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val netCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            if (netCapabilities != null) {
                when {
                    netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        isConnected = true
                    }
                    netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        isConnected = true
                    }
                    netCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        isConnected = true
                    }
                }
            }
        } else {
            val activeNetworkInfo = cm.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                isConnected = true
            }
        }
        return isConnected
    }

    // Настройка ActionBar и обработка нажатия на стрелку назад
    private fun adjustActionBar() {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_action_bar)
        val navIcon = supportActionBar?.customView?.findViewById<ImageView>(R.id.nav_icon)
        navIcon?.setOnClickListener {
            hideList()
            showButtonAndText()
            setTextToButton()
        }
    }

    companion object {
        var baseUrl = "https://jsonplaceholder.typicode.com/"
    }
}