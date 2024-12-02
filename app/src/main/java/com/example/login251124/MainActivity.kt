package com.example.login251124

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login251124.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private  val responseLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== RESULT_OK){
            val datos=GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val cuenta=datos.getResult(ApiException::class.java)
                if(cuenta!=null){
                    val credenciales=GoogleAuthProvider.getCredential(cuenta.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credenciales)
                        .addOnCompleteListener{
                            if(it.isSuccessful){
                                irActivityApp()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                }
            }catch(e: ApiException){
                Log.d("ERROR de API:>>>>", e.message.toString())
            }
        }
        if(it.resultCode== RESULT_CANCELED){
            Toast.makeText(this, "El usuario canceló", Toast.LENGTH_SHORT).show()
        }
    }


    private lateinit var binding : ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var email=""
    private var pass=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth=Firebase.auth
        setListeners()
    }
    //----------------------------------------------------------------------------------------------
    private fun setListeners() {
        binding.btnReset.setOnClickListener {
            limpiar()
        }
        binding.btnLogin.setOnClickListener {
            login()
        }
        binding.btnRegister.setOnClickListener {
            registrar()
        }
        binding.btnGoogle.setOnClickListener{
            loginGoogle()
        }
    }
    //----------------------------------------------------------------------------------------------
    private fun loginGoogle() {
        val googleConf=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        val googleClient=GoogleSignIn.getClient(this, googleConf)

        googleClient.signOut() //Fundamental para que no haga login automatico si he cerrado session

        responseLauncher.launch(googleClient.signInIntent)
    }

    //----------------------------------------------------------------------------------------------
    private fun registrar() {
        if(!datosCorrectos()) return
        //datos correctos, procedemos a registar al usuario
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    //si el usuario se ha creado vamos a iniciar sesion con el
                    login()
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    //----------------------------------------------------------------------------------------------
    private fun login() {
        if(!datosCorrectos()) return
        //LOs datos ya estan validados
        //vamos a logear al usuario
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    //todo ha ido bien
                    irActivityApp()
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
    //----------------------------------------------------------------------------------------------

    private fun datosCorrectos(): Boolean {
        email=binding.etEmail.text.toString().trim()
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error="Se esperaba una direccion de email correcta."
            return false
        }
        pass=binding.etPass.text.toString().trim()
        if(pass.length<6){
            binding.etPass.error="Error, la contraseña debe tener al menos 6 caracteres"
            return false
        }
        return true
    }
    //----------------------------------------------------------------------------------------------

    private fun limpiar() {
        binding.etPass.setText("")
        binding.etEmail.setText("")
    }

    private fun irActivityApp(){
        startActivity(Intent(this, AppActivity::class.java))
    }

    override fun onStart() {
        //Si ya tengo sesión iniciada nos saltamos el login
        super.onStart()
        val usuario=auth.currentUser
        if(usuario!=null) irActivityApp()
    }
}