package com.example.movies.utlis

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.movies.AddBookFragment
import com.example.movies.MainFragment
import com.example.movies.databinding.BookRowBinding
import com.example.movies.databinding.FragmentMainBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BooksAdapter(private val list: MutableList<BooksData>) : RecyclerView.Adapter<BooksAdapter.BooksViewHolder>() {

    private  val TAG = "BooksAdapter"
    private var listener: BookAdapterClicksInterface? = null
    fun setListener(listener: BookAdapterClicksInterface){
        this.listener = listener
    }

    class BooksViewHolder(val binding: BookRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val binding = BookRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        var _binding = FragmentMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.readedOrNotCheckBox.setOnClickListener {
            _binding.addBook.callOnClick()
        }

        return BooksViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        with(holder){
            with(list[position]){

                val bookObject = this.bookObject

                binding.title.text = bookObject!!.bookTitle
                binding.author.text = bookObject.bookAuthor
                binding.readedOrNotCheckBox.isChecked = bookObject.readed

                Log.d(TAG, "onBindViewHolder: $this")
                binding.deleteBookButton.setOnClickListener{
                    listener?.onDeleteBtnClicked(this)
                }

                binding.editBookButton.setOnClickListener{
                    listener?.onEditBtnClicked(this)
                }
            }
        }
    }

    interface BookAdapterClicksInterface{
        fun onDeleteBtnClicked(booksData: BooksData)
        fun onEditBtnClicked(booksData: BooksData)
    }
}