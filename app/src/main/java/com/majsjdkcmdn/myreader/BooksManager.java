package com.majsjdkcmdn.myreader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


//该类负责管理书籍列表并呈现视图
public class BooksManager extends RecyclerView.Adapter<BooksManager.BookViewHolder> {

    //属性 待补充
    public List<Book> Book_list = new ArrayList<>();
    public List<Book> Favorite_list = new ArrayList<>();
    private static OnBookCoverClickListener onBookCoverClickListener;

    public static void renew() {
        //TODO
        Log.v("Good", "renew");
    }

    public interface OnBookCoverClickListener {
        void onBookCoverClick(int position);
    }

    public void setOnBookCoverClickListener(OnBookCoverClickListener listener) {
        onBookCoverClickListener = listener;
    }
    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCover;
        TextView textViewTitle;
        TextView textViewAuthor;
        TextView textViewProgress;
        ImageView imageViewFavorite;
        ImageView imageViewDelete;
        private static final int REQUEST_CODE_IMPORT_FILE = 1;

        public BookViewHolder(View view) {
            super(view);
            imageViewCover = view.findViewById(R.id.book_cover);
            textViewTitle = view.findViewById(R.id.book_title);
            textViewAuthor = view.findViewById(R.id.book_author);
            textViewProgress = view.findViewById(R.id.book_progress);
            imageViewFavorite = view.findViewById(R.id.book_favourite);
            imageViewDelete = view.findViewById(R.id.book_delete);
            imageViewCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onBookCoverClickListener != null){
                        onBookCoverClickListener.onBookCoverClick(getBindingAdapterPosition());
                    }
                }
            });
        }
    }
    public BooksManager(){
        Book_list.add(new Book());
        Book_list.add(new Book());
        Book_list.add(new Book());
        Book_list.add(new Book());
        Book_list.add(new Book());
        Book_list.add(new Book());
    }
    //管理器类初始化，从指定文件中读取相关列表并赋值
    //文件存储书籍对应的drawable名字，减小内存消耗
    //文件可以选用SQLite数据库文件


    private void toggleFavorite(Book book) {


    }
    //处理喜爱，设置activate标签并加入喜欢列表

    private void deleteBook(int position) {
    }

    public List<Book> getBooks(){


        return Book_list;
    }


    @Override
    public int getItemCount() {
        return Book_list.size();
    }


    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_list, parent, false);
        return new BookViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = Book_list.get(position);
        holder.imageViewCover.setImageResource(book.getCover());
        holder.textViewTitle.setText(book.getTitle());
        holder.textViewAuthor.setText(book.getAuthor());
        holder.textViewProgress.setText(book.getProgress());
        holder.imageViewFavorite.setOnClickListener(v -> toggleFavorite(book));
        holder.imageViewDelete.setOnClickListener(v->deleteBook(position));
    }



}
