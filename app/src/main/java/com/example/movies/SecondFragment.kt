package com.example.movies

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.movies.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        _binding!!.buttonRegister.setOnClickListener{
            val email = _binding!!.registerEmail.text.toString()
            val pass = _binding!!.registerPass.text.toString()
            val pass2 = _binding!!.registerPass2.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty() && pass2.isNotEmpty()){
                if(pass == pass2){
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                        if(it.isSuccessful){
                            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                        }else{
                            Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(context, "Hasła nie są takie same!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "Nie wszystkie pola są uzupełnione!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}