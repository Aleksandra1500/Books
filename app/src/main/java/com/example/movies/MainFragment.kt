package com.example.movies

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movies.databinding.FragmentMainBinding
import com.example.movies.utlis.BookObject
import com.example.movies.utlis.BooksAdapter
import com.example.movies.utlis.BooksData
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainFragment : Fragment(), AddBookFragment.DialogNextBtnClickListener,
    BooksAdapter.BookAdapterClicksInterface {

    private lateinit var _binding: FragmentMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private var addBookFragment: AddBookFragment? = null

    private lateinit var adapterToRead: BooksAdapter
    private lateinit var adapterReaded: BooksAdapter
    private lateinit var adapterFilter: BooksAdapter

    private lateinit var toReadList: MutableList<BooksData>
    private lateinit var readedList: MutableList<BooksData>
    private lateinit var filterList: MutableList<BooksData>

    private var emptyTextInput: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        getDataFromFireBase()
        registerEvents()

        val spinner = _binding.spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.Lengths,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
        }

        _binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    _binding.toReadList.visibility = View.VISIBLE
                    _binding.readedList.visibility = View.INVISIBLE
                    _binding.filterList.visibility = View.INVISIBLE
                    _binding.spinner.visibility = View.INVISIBLE
                    _binding.addBook.visibility = View.VISIBLE
                } else if (tab?.position == 1) {
                    _binding.toReadList.visibility = View.INVISIBLE
                    _binding.readedList.visibility = View.VISIBLE
                    _binding.filterList.visibility = View.INVISIBLE
                    _binding.spinner.visibility = View.INVISIBLE
                    _binding.addBook.visibility = View.VISIBLE
                } else{
                    _binding.toReadList.visibility = View.INVISIBLE
                    _binding.readedList.visibility = View.INVISIBLE
                    _binding.filterList.visibility = View.VISIBLE
                    _binding.spinner.visibility = View.VISIBLE
                    _binding.addBook.visibility = View.INVISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun registerEvents(){
        _binding.addBook.setOnClickListener {
            if(addBookFragment != null)
                childFragmentManager.beginTransaction().remove(addBookFragment!!).commit()
            addBookFragment = AddBookFragment()
            addBookFragment!!.setListener(this)
            addBookFragment!!.show(
                childFragmentManager,
                AddBookFragment.TAG
            )
        }

        _binding.spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                getDataFromFireBase()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Books").child(firebaseAuth.currentUser?.uid.toString())

        toReadList = mutableListOf()
        readedList = mutableListOf()
        filterList = mutableListOf()

        _binding.toReadList.setHasFixedSize(true)
        _binding.toReadList.layoutManager = LinearLayoutManager(context)
        adapterToRead = BooksAdapter(toReadList)
        adapterToRead.setListener(this)
        _binding.toReadList.adapter = adapterToRead

        _binding.readedList.setHasFixedSize(true)
        _binding.readedList.layoutManager = LinearLayoutManager(context)
        adapterReaded = BooksAdapter(readedList)
        adapterReaded.setListener(this)
        _binding.readedList.adapter = adapterReaded

        _binding.filterList.setHasFixedSize(true)
        _binding.filterList.layoutManager = LinearLayoutManager(context)
        adapterFilter = BooksAdapter(filterList)
        adapterFilter.setListener(this)
        _binding.filterList.adapter = adapterFilter
    }

    private fun getDataFromFireBase(){
        databaseRef.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                toReadList.clear()
                readedList.clear()
                filterList.clear()
                for (taskSnapshot in snapshot.children){
                    val book = BooksData()
                    book.bookId = taskSnapshot.key.toString()
                    val temp = taskSnapshot.value
                    val temp2 = temp as HashMap<*, *>
                    book.bookObject = BookObject(
                        temp2.get("bookTitle").toString(),
                        temp2.get("bookAuthor").toString(),
                        temp2.get("readed") as Boolean,
                        temp2.get("length") as Long)

                    if(!book.bookObject!!.readed){
                        toReadList.add(book)
                    }
                    else{
                        readedList.add(book)
                    }

                    if(_binding.spinner.selectedItem.toString() == "Krótkie" && book.bookObject!!.length!! <= 100)
                    {
                        filterList.add(book)
                    }
                    else if(_binding.spinner.selectedItem.toString() == "Średnie" && book.bookObject!!.length!! > 100 && book.bookObject!!.length!! <= 350)
                    {
                        filterList.add(book)
                    }
                    else if(_binding.spinner.selectedItem.toString() == "Długie" && book.bookObject!!.length!! > 350)
                    {
                        filterList.add(book)
                    }
                    else if(_binding.spinner.selectedItem.toString() == "Wszystko")
                    {
                        filterList.add(book)
                    }
                }
                adapterToRead.notifyDataSetChanged()
                adapterReaded.notifyDataSetChanged()
                adapterFilter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onSaveBook(
        bookObject: BookObject,
        bookTitleInput: TextInputEditText,
        bookAuthorInput: TextInputEditText,
        bookLengthInput : TextInputEditText
    ) {
        databaseRef.push().setValue(bookObject).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Zapisano", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            bookTitleInput.text = null
            bookAuthorInput.text = null
            addBookFragment!!.dismiss()
        }
    }

    override fun onUpdateBook(
        booksData: BooksData,
        bookTitleInput: TextInputEditText?,
        bookAuthorInput: TextInputEditText?,
        bookLengthInput : TextInputEditText?
    ) {
        val map = HashMap<String, Any>()
        map[booksData.bookId] = booksData.bookObject!!
        databaseRef.updateChildren(map).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Nadpisano", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            if (bookTitleInput != null) {
                bookTitleInput.text = null
            }
            if (bookAuthorInput != null) {
                bookAuthorInput.text = null
            }
            if (bookLengthInput != null) {
                bookLengthInput.text = null
            }
            if(addBookFragment != null){
                addBookFragment!!.dismiss()
            }
        }
    }

    override fun onDeleteBtnClicked(booksData: BooksData) {
        databaseRef.child(booksData.bookId).removeValue().addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Usunięto", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditBtnClicked(booksData: BooksData) {
        if (addBookFragment != null)
            childFragmentManager.beginTransaction().remove(addBookFragment!!).commit()

        addBookFragment = AddBookFragment.newInstance(booksData.bookId, booksData.bookObject)
        addBookFragment!!.setListener(this)
        addBookFragment!!.show(childFragmentManager, AddBookFragment.TAG)
    }

    override fun onCheckBoxBtnClicked(booksData: BooksData, checked: Boolean) {
        booksData.bookObject?.readed = checked
        onUpdateBook(booksData, emptyTextInput, emptyTextInput, emptyTextInput)
    }
}