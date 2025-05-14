import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trashsense.R
import com.example.trashsense.PostAdapter
import com.example.trashsense.home.Post_Data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postData: ArrayList<Post_Data>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        postData = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.homefragment_rview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostAdapter(postData)
        recyclerView.adapter = adapter

        loadPosts()

        return view
    }

    private fun loadPosts() {
        db.collection("Posts")
            .document("Data")
            .collection("Aayush")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val pfUrl = document.getString("Profile_url") ?: ""
                    val text = document.getString("text") ?: ""
                    val imageUrl = document.getString("url") ?: ""
                    val username = document.getString("Username") ?: "Gurjar"

                    val post = Post_Data(pfUrl, text, imageUrl,username)
                    postData.add(post)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }
}
