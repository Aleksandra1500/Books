package com.example.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.movies.databinding.BookRowBinding
import com.example.movies.databinding.FragmentAddBookBinding
import com.example.movies.utlis.BookObject
import com.example.movies.utlis.BooksData
import com.google.android.material.textfield.TextInputEditText


class AddBookFragment : DialogFragment() {

    private lateinit var _binding : FragmentAddBookBinding
    private lateinit var bindingBookRow : BookRowBinding
    private lateinit var listener : DialogNextBtnClickListener
    private var booksData : BooksData? = null

    fun setListener(listener: DialogNextBtnClickListener){
        this.listener = listener
    }

    companion object{
        const val TAG = "AddBookFragment"
        @JvmStatic
        fun newInstance(bookId:String, bookObject:BookObject?) = AddBookFragment().apply {
            arguments = Bundle().apply {
                putString("bookId", bookId)
                putParcelable("bookObject", bookObject)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)

        bindingBookRow = BookRowBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null){
            booksData = BooksData(arguments?.getString("bookId").toString(),
                arguments?.getParcelable("bookObject"))

            var bookObject = booksData!!.bookObject
            if (bookObject != null) {
                _binding.addTitle.setText(bookObject.bookTitle)
                _binding.addAuthor.setText(bookObject.bookAuthor)
            }
        }

        registerEvents()

        bindingBookRow.readedOrNotCheckBox.setOnClickListener{
            val readed : Boolean
            readed = bindingBookRow.readedOrNotCheckBox.isChecked
            val bookObject = BookObject("","",readed)
            val bookId = arguments?.getString("bookId").toString()
            val currentBookData = BooksData(bookId, bookObject)

            listener.onUpdateBook(currentBookData, _binding.addTitle, _binding.addAuthor)
        }
    }

    private fun registerEvents(){
        _binding.saveButton.setOnClickListener {
            val bookTitle = _binding.addTitle.text.toString()
            val bookAuthor = _binding.addAuthor.text.toString()

            val bookObject = BookObject(bookTitle, bookAuthor, false)

            if(bookTitle.isNotEmpty() && bookAuthor.isNotEmpty()){

                if(booksData == null){
                    listener.onSaveBook(bookObject, _binding.addTitle, _binding.addAuthor)
                }
                else{
                    val temp = booksData?.bookObject?.readed
                    booksData?.bookObject = bookObject
                    booksData?.bookObject?.readed = temp!!
                    listener.onUpdateBook(booksData!!, _binding.addTitle, _binding.addAuthor)
                }

            }else{
                Toast.makeText(context, "Wszystkie pola muszą być uzupełnione", Toast.LENGTH_SHORT).show()
            }
        }

        _binding.closeAddingButton.setOnClickListener{
            dismiss()
        }
    }

    interface DialogNextBtnClickListener{
        fun onSaveBook(bookObject : BookObject, bookTitleInput : TextInputEditText, bookAuthorInput : TextInputEditText)
        fun onUpdateBook(booksData: BooksData, bookTitleInput : TextInputEditText, bookAuthorInput : TextInputEditText)
    }
}