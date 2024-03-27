package edu.uncc.assignment09;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.uncc.assignment09.databinding.FragmentPostsBinding;
import edu.uncc.assignment09.databinding.PostRowItemBinding;
import edu.uncc.assignment09.models.Post;

public class PostsFragment extends Fragment {

    public static final String TAG = "debug";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public PostsFragment() {
        // Required empty public constructor
    }

    FragmentPostsBinding binding;
    PostsAdapter postsAdapter;
    ArrayList<Post> mPosts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userName = mAuth.getCurrentUser().getDisplayName();

        binding.textViewTitle.setText("Welcome, " + userName);

        binding.buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.createPost();
            }
        });

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logout();
            }
        });

        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postsAdapter = new PostsAdapter();
        binding.recyclerViewPosts.setAdapter(postsAdapter);
        getActivity().setTitle(R.string.posts_label);

        getPostsCollection();
    }

    void getPostsCollection(){
        // Get posts from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("postCollection").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mPosts.clear();

                            for (QueryDocumentSnapshot doc: task.getResult()) {
                                Post post = doc.toObject(Post.class);
                                mPosts.add(post);
                                //Log.d(TAG, "QueryDocumentSnapshot: " + post);
                                //Log.d(TAG, "QueryDocumentSnapshot: " + doc.getId());
                            }

                            postsAdapter.notifyDataSetChanged();

                        } else {
                            Log.d(TAG, "Fetch Data Failed msg: " + task.getException().getMessage());
                        }
                    }
                });
    }

    class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {
        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PostRowItemBinding binding = PostRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new PostsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {
            Post post = mPosts.get(position);
            holder.setupUI(post);
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        class PostsViewHolder extends RecyclerView.ViewHolder {
            PostRowItemBinding mBinding;
            Post mPost;
            public PostsViewHolder(PostRowItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Post post){
                mPost = post;
                mBinding.textViewPost.setText(post.getPostText());
                mBinding.textViewCreatedBy.setText(post.getCreatedByName());

                // Set the created at time
                if(post.getCreatedAt() == null) {
                    mBinding.textViewCreatedAt.setText("N/A");
                } else {
                    Date date = post.getCreatedAt().toDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    mBinding.textViewCreatedAt.setText(sdf.format(date));
                }

                if (mAuth.getCurrentUser().getUid().equals(mPost.getCreatedByUid())){

                    mBinding.imageViewDelete.setVisibility(View.VISIBLE);

                    mBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setTitle("Delete Post?").setMessage(mPost.getPostText());
                            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // delete post and re-fetch the data
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("postCollection").document(mPost.getDocId())
                                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Post Deleted Success!");
                                                        getPostsCollection(); // Fetch the data gain
                                                    } else {
                                                        Log.d(TAG, "Post Not Deleted!");
                                                    }
                                                }
                                            });
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "NegativeButton Clicked!");
                                    }
                                });

                            alertDialog.create().show();
                        }
                    });
                } else {
                    mBinding.imageViewDelete.setVisibility(View.INVISIBLE);
                }

            }
        }

    }

    PostsListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (PostsListener) context;
    }

    interface PostsListener{
        void logout();
        void createPost();
    }
}