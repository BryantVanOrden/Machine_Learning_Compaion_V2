package np.com.bryant.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import np.com.bryant.myapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var auth: FirebaseAuth = FirebaseAuth.getInstance() // Initialize Firebase Authentication

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener // Define the authStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize the authStateListener
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                // User is already logged in, navigate to MainActivity or your desired destination
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }


        // Initialize the Firebase Authentication state listener
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                // User is already logged in, navigate to MainActivity or your desired destination
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Add the listener
        auth.addAuthStateListener(authStateListener)

        binding.loginBtn.setOnClickListener {
            login()
        }

        binding.goToSignupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Password reset button
        binding.forgotPasswordBtn.setOnClickListener {
            resetPassword()
        }
    }

    // ...

    // Ensure you remove the listener when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun login() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Email not valid"
            return
        }

        if (password.isEmpty()) {
            binding.passwordInput.error = "Password is required"
            return
        }

        // Perform Firebase login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success (handled by the AuthStateListener)
                } else {
                    // If sign-in fails, display a message to the user.
                    Toast.makeText(
                        applicationContext, "Incorrect email or password, try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun resetPassword() {
        val email = binding.emailInput.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Email not valid"
            return
        }

        // Send a password reset email
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Password reset email sent.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Failed to send password reset email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}