package com.majsjdkcmdn.myreader;


import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;


//该类负责管理书籍列表并呈现视图
public class BooksManager extends ListAdapter<Book, BooksManager.BookViewHolder> {

    //属性 待补充
    // TODO
    int i = 0;

    public List<Book> Book_list = new ArrayList<>();
    public List<Book> Favorite_list = new ArrayList<>();
    Resources res;
    private static OnBookCoverClickListener onBookCoverClickListener;
    private static OnBookModifyClickListener onBookModifyClickListener;
    private static OnBookLikeClickListener onBookLikeClickListener;
    private static OnBookDeleteClickListener onBookDeleteClickListener;



    public BooksManager(Resources res) throws IOException{
        //TODO
        super(new BookDiff());
        this.res = res;
    }
    //管理器类初始化，从指定文件中读取相关列表并赋值
    //文件可以选用SQLite数据库文件
    public BooksManager(Resources res, File Dic) throws IOException {
        //TODO
        super(new BookDiff());
        this.res = res;
        File[] files = Dic.listFiles();
        assert files != null;
        for(File file:files){
            String name = file.getAbsolutePath();
            if(name.substring(name.lastIndexOf(".") + 1).equals("epub")) {
                ZipFile zipFile = new ZipFile(file);
                Book_list.add(new Book(res, i++, zipFile, name));
                zipFile.close();
            }
        }
    }
    public Book renew(String name) throws IOException {
        //TODO
        Book book;
        if(name.substring(name.lastIndexOf(".") + 1).equals("epub")){
            ZipFile zipFile = new ZipFile(name);
            book = new Book(res, i++, zipFile, name);
            zipFile.close();
        }
        else{
            book = new Book(res, i++);
        }
        Book_list.add(book);
        Log.v("Good", "renew");
        return book;
    }

    public List<Book> deleteBook(int position){
        //TODO
        Book_list.remove(position);
        return Book_list;
    }

    private void toggleFavorite(int position) {
        //TODO
        Log.v("like", String.valueOf(position));
    }
    //处理喜爱，设置activate标签并加入喜欢列表

    public interface OnBookCoverClickListener {
        void onBookCoverClick(int position);
    }

    public interface OnBookModifyClickListener {
        void onBookModifyClick(int position);
    }

    public interface OnBookLikeClickListener {
        void onBookLikeClick(int position);
    }

    public interface OnBookDeleteClickListener {
        void onBookDeleteClick(int position);
    }

    public void setOnBookCoverClickListener(OnBookCoverClickListener listener) {
        onBookCoverClickListener = listener;
    }
    public void setOnBookModifyClickListener(OnBookModifyClickListener listener) {
        onBookModifyClickListener = listener;
    }
    public void setOnBookLikeClickListener(OnBookLikeClickListener listener) {
        onBookLikeClickListener = listener;
    }
    public void setOnBookDeleteClickListener(OnBookDeleteClickListener listener) {
        onBookDeleteClickListener = listener;
    }
    static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCover;
        TextView textViewTitle;
        TextView textViewProgress;
        ImageView imageViewModify;
        ImageView imageViewFavorite;
        ImageView imageViewDelete;

        public BookViewHolder(View view) {
            super(view);
            imageViewCover = view.findViewById(R.id.book_cover);
            textViewTitle = view.findViewById(R.id.book_title);
            textViewProgress = view.findViewById(R.id.book_progress);
            imageViewModify = view.findViewById(R.id.book_modify);
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
            imageViewModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onBookModifyClickListener != null){
                        onBookModifyClickListener.onBookModifyClick(getBindingAdapterPosition());
                    }
                }
            });
            imageViewFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onBookLikeClickListener != null){
                        onBookLikeClickListener.onBookLikeClick(getBindingAdapterPosition());
                    }
                }
            });
            imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onBookDeleteClickListener != null){
                        onBookDeleteClickListener.onBookDeleteClick(getBindingAdapterPosition());
                    }
                }
            });

        }
    }

    public static class BookDiff extends DiffUtil.ItemCallback<Book> {
        @Override
        public boolean areItemsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
            // 判断两个对象是否表示同一个书籍
            return oldItem.ID == newItem.ID;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
            // 判断两个对象的内容是否相同
            return oldItem.equal(newItem);
        }
    }

    public void submitList(List<Book> books) {
        super.submitList(new ArrayList<>(books));
    }

    @Override
    public int getItemCount() {
        return Book_list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = Book_list.get(position);
        holder.imageViewCover.setImageDrawable(book.Cover);
        holder.textViewTitle.setText(book.Title);
        holder.textViewProgress.setText(book.Progress);
        holder.imageViewFavorite.setActivated(book.Like);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_list, parent, false);
        return new BookViewHolder(view);
    }
}
