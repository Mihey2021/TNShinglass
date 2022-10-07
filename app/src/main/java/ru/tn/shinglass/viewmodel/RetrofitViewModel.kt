package ru.tn.shinglass.viewmodel
//
//import android.app.Application
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.ServiceConnection
//import android.os.IBinder
//import android.widget.Toast
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import ru.tn.shinglass.R
////import ru.tn.shinglass.api.Service1C
//
//class RetrofitViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val _connectionService1C: MutableLiveData<Service1C?> = MutableLiveData(null)
//    val connectionService1C: LiveData<Service1C?>
//        get() = _connectionService1C
//
//
//    fun serviceInit(context: Context) {
//        val intent = Intent(context, Service1C::class.java)
//        //_basicPrefs.value = PreferenceManager.getDefaultSharedPreferences(context)
//        val mServiceConnection = object : ServiceConnection {
//            override fun onServiceConnected(cName: ComponentName?, service: IBinder?) {
//                val serviceBinder = service as Service1C.Service1CBLBinder
//                _connectionService1C.value = serviceBinder.getService()
//                Toast.makeText(
//                    context,
//                    R.string.https_service_connected,
//                    Toast.LENGTH_SHORT
//                ).show()
//                val mServiceBound = true
//            }
//
//            override fun onServiceDisconnected(p0: ComponentName?) {
//                //_basicPrefs.value = PreferenceManager.getDefaultSharedPreferences(context)
//                val mServiceBound = false;
//            }
//        }
//        //context.unbindService(mServiceConnection)
//        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
//    }
//
//}