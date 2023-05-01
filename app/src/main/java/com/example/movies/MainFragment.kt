package com.example.movies

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import java.lang.reflect.TypeVariable


class MainFragment : Fragment(), AddBookFragment.DialogNextBtnClickListener,
    BooksAdapter.BookAdapterClicksInterface {

    private lateinit var _binding: FragmentMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController
    private var addBookFragment: AddBookFragment? = null

    private lateinit var adapterToRead: BooksAdapter
    private lateinit var adapterReaded: BooksAdapter

    private lateinit var toReadList: MutableList<BooksData>
    private lateinit var readedList: MutableList<BooksData>

    private lateinit var bookObject: BookObject

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

        _binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    _binding.toReadList.visibility = View.VISIBLE
                    _binding.readedList.visibility = View.INVISIBLE
                } else {
                    _binding.toReadList.visibility = View.INVISIBLE
                    _binding.readedList.visibility = View.VISIBLE
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
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference
            .child("Books").child(firebaseAuth.currentUser?.uid.toString())

        toReadList = mutableListOf()
        readedList = mutableListOf()

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
    }

    private fun getDataFromFireBase(){
        databaseRef.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                toReadList.clear()
                readedList.clear()
                for (taskSnapshot in snapshot.children){
                    val book = BooksData()
                    book.bookId = taskSnapshot.key.toString()
                    val temp = taskSnapshot.value
                    val temp2 = temp as HashMap<*, *>
                    book.bookObject = BookObject(
                        temp2.get("bookTitle").toString(),
                        temp2.get("bookAuthor").toString(),
                        temp2.get("readed") as Boolean)

                    if(!book.bookObject!!.readed){
                        toReadList.add(book)
                    }
                    else{
                        readedList.add(book)
                    }
                }
                adapterToRead.notifyDataSetChanged()
                adapterReaded.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onSaveBook(
        bookObject: BookObject,
        bookTitleInput: TextInputEditText,
        bookAuthorInput: TextInputEditText
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
        bookTitleInput: TextInputEditText,
        bookAuthorInput: TextInputEditText
    ) {
        val map = HashMap<String, Any>()
        map[booksData.bookId] = booksData.bookObject!!
        databaseRef.updateChildren(map).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context, "Nadpisano", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            bookTitleInput.text = null
            bookAuthorInput.text = null
            addBookFragment!!.dismiss()
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
}