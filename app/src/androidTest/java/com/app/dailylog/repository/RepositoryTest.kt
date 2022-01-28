package com.app.dailylog.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.ui.permissions.PermissionChecker
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {

    lateinit var repository: Repository

//    @Test
//    fun should_Insert_Food_Item() {
//        val food = Food(1, "Banana", "Desc")
//        foodDao?.insertFood(food)
//        val foodTest = getValue(foodDao?.getFood(food.foodId)!!)
//        Assert.assertEquals(food.foodName, foodTest.foodName)
//    }
//
//    @Test
//    fun should_Flush_All_Data(){
//        foodDao?.flushFoodData()
//        Assert.assertEquals(foodDao?.getFoodsCount(), 0)
//    }
//
//    // Copied from stackoverflow
//    @Throws(InterruptedException::class)
//    fun <T> getValue(liveData: LiveData<T>): T {
//        val data = arrayOfNulls<Any>(1)
//        val latch = CountDownLatch(1)
//        val observer = object : Observer<T> {
//            override fun onChanged(t: T?) {
//                data[0] = t
//                latch.countDown()
//                liveData.removeObserver(this)//To change body of created functions use File | Settings | File Templates.
//            }
//
//        }
//        liveData.observeForever(observer)
//        latch.await(2, TimeUnit.SECONDS)
//
//        return data[0] as T
//    }

    @Before
    fun setUp() {
        ShortcutDatabase.TEST_MODE = true
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = Repository(context, PermissionChecker(null))
        repository.shortcutDao = ShortcutDatabase.getDatabase(context).shortcutDao()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getDateTimeFormat() {
//        Assert.assertEquals(Constants.DATE_TIME_DEFAULT_FORMAT, repository.getDateTimeFormat())
    }

    @Test
    fun setsDateTimeFormat() {
    }

    @Test
    fun getCursorIndex() {
    }

    @Test
    fun setCursorIndex() {
    }

    @Test
    fun getAllShortcuts() {
    }

    @Test
    fun labelIsUnique() {
    }

    @Test
    fun updateShortcut() {
    }

    @Test
    fun updateShortcutList() {
    }

    @Test
    fun getContext() {
    }

    @Test
    fun getPermissionChecker() {
    }
}