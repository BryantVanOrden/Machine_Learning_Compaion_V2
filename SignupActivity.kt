package np.com.bryant.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import np.com.bryant.myapp.databinding.ActivitySignupBinding
import np.com.bryant.myapp.model.UserModel
import np.com.bryant.myapp.util.UiUtil
import com.google.firebase.firestore.ktx.firestore


class SignupActivity : AppCompatActivity() {

    lateinit var binding : ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.submitBtn.setOnClickListener{
            signup()
        }

        binding.goToLoginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    fun setInProgress(inProgress : Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.submitBtn.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.submitBtn.visibility = View.VISIBLE
        }
    }



    fun signup(){
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.setError("Email not valid")
            return;
        }
        if(password.length<6){
            binding.passwordInput.setError("Password must be between 6-20 characters")
            return;
        }
        if(password.length>=20){
            binding.passwordInput.setError("Password must be between 6-20 characters")
            return;
        }

        if(password!=confirmPassword) {
            binding.confirmPasswordInput.setError("Passwords must be the same")
            return;
        }
        signupWithFirebase(email,password)
    }

    fun signupWithFirebase(email: String, password: String) {
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let {user ->
                    val userModel = UserModel(user.uid,email,email.substringBefore("@"))
                    Firebase.firestore.collection("users")
                        .document(user.uid)
                        .set(userModel).addOnSuccessListener{
                            UiUtil.showToast(applicationContext, "Account created successfully")
                            setInProgress(false)
                            startActivity(Intent(this,MainActivity::class.java))
                            finish()
                        }
                }
                // Successful authentication
                Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle authentication failure
                Toast.makeText(applicationContext, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                setInProgress(false) // Make sure to hide the progress bar
            }
    }
}