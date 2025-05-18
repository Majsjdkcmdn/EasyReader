package com.majsjdkcmdn.myreader;


import android.content.res.Resources;
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

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipFile;


//该类负责管理书籍列表并呈现视图
public class BooksManager extends ListAdapter<Book, BooksManager.BookViewHolder> {
    public static class DatabaseData{
        private String ID;
        private String Title;
        private Boolean Like;
        private int LastPage;
        private String FilePath;
        public DatabaseData(){
        }
        public DatabaseData(String id, String title, Boolean like, int lastPage, String filePath){
            ID = id;
            Title = title;
            Like = like;
            LastPage = lastPage;
            FilePath = filePath;
        }
        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getTitle() {
            return Title;
        }

        public void setTitle(String title) {
            Title = title;
        }

        public Boolean getLike() {
            return Like;
        }

        public void setLike(Boolean like) {
            Like = like;
        }

        public int getLastPage() {
            return LastPage;
        }

        public void setLastPage(int lastPage) {
            LastPage = lastPage;
        }

        public String getFilePath() {
            return FilePath;
        }

        public void setFilePath(String filePath) {
            FilePath = filePath;
        }
    }

    public List<Book> Book_list = new ArrayList<>();
    Resources res;
    File Database;
    List<DatabaseData> dataList_updated = new ArrayList<>();
    boolean found_flag = false;
    private static OnBookCoverClickListener onBookCoverClickListener;
    private static OnBookModifyClickListener onBookModifyClickListener;
    private static OnBookLikeClickListener onBookLikeClickListener;
    private static OnBookDeleteClickListener onBookDeleteClickListener;

    public BooksManager(Resources res, File Database) throws IOException{
        super(new BookDiff());
        this.res = res;
        this.Database = Database;
    }
    //管理器类初始化，从指定文件中读取相关列表并赋值
    //文件可以选用SQLite数据库文件
    public BooksManager(Resources res, File Dic, File Database) throws IOException {
        //TODO
        super(new BookDiff());
        this.res = res;
        this.Database = Database;

        List<DatabaseData> dataList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(Database))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                DatabaseData data=JSON.parseObject(line,DatabaseData.class);
                dataList.add(data);
            }
        } catch (Exception e) {
            Log.e(e.getMessage(), String.valueOf(e));
        }
        File[] files = Dic.listFiles();
        assert files != null;

        for (File file : files) {
            String name = file.getAbsolutePath();
            Book book;
            found_flag = false;
            if (name.substring(name.lastIndexOf(".") + 1).equals("epub")) {
                ZipFile zipFile = new ZipFile(file);
                for(DatabaseData item:dataList){
                    if(Objects.equals(item.FilePath, name)){
                        book = new Book(res, item.ID, zipFile, name);
                        book.Title = item.Title;
                        book.Like = item.Like;
                        book.LastPage = item.LastPage;
                        Book_list.add(book);
                        found_flag = true;
                        break;
                    }
                }
                if(!found_flag){
                    book = new Book(res, UUID.randomUUID().toString(), zipFile, name);
                    Book_list.add(book);
                }
                zipFile.close();
            }
            else{
                found_flag = false;
                for(DatabaseData item:dataList){
                    if(Objects.equals(item.FilePath, name)){
                        book = new Book(res, item.ID);
                        book.FilePath = item.FilePath;
                        book.Title = item.Title;
                        book.Like = item.Like;
                        book.LastPage = item.LastPage;
                        Book_list.add(book);
                        found_flag = true;
                        break;
                    }
                }
                if(!found_flag){
                    book = new Book(res, UUID.randomUUID().toString());
                    book.FilePath = name;
                    Book_list.add(book);
                }
            }
        }

        for(Book book:Book_list){
            DatabaseData data = new DatabaseData(book.ID, book.Title, book.Like, book.LastPage, book.FilePath);
            dataList_updated.add(data);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(Database));
        for(DatabaseData data:dataList_updated){
            Log.v("sys", JSON.toJSONString(data));
            writer.write(JSON.toJSONString(data));
            writer.newLine();
        }
        writer.close();
    }

    public void importBook(String name) throws IOException {
        //TODO
        Log.v("ImportMessage", "Importing");
        Book book;
        if(name.substring(name.lastIndexOf(".") + 1).equals("epub")){
            found_flag = false;
            for(DatabaseData item:dataList_updated){
                if(Objects.equals(item.FilePath, name)){
                    found_flag = true;
                    break;
                }
            }
            if(!found_flag){
                ZipFile zipFile = new ZipFile(name);
                book = new Book(res, UUID.randomUUID().toString(), zipFile, name);
                zipFile.close();
                Book_list.add(book);
                Log.v("ImportType", String.valueOf(book.FileClass));

                BufferedWriter writer = new BufferedWriter(new FileWriter(Database,true));
                DatabaseData data = new DatabaseData(book.ID, book.Title, book.Like, book.LastPage, book.FilePath);
                dataList_updated.add(data);
                writer.write(JSON.toJSONString(data));
                writer.newLine();
                writer.close();
            }
            else Log.v("ImportMessage", "ExistedFile");
        }
        else{
            found_flag = false;
            for(DatabaseData item:dataList_updated){
                if(Objects.equals(item.FilePath, name)){
                    found_flag = true;
                    break;
                }
            }
            if(!found_flag){
                book = new Book(res, UUID.randomUUID().toString());
                book.FilePath = name;
                Book_list.add(book);
                Log.v("ImportType","TempFile");

                BufferedWriter writer = new BufferedWriter(new FileWriter(Database,true));
                DatabaseData data = new DatabaseData(book.ID, book.Title, book.Like, book.LastPage, book.FilePath);
                dataList_updated.add(data);
                writer.write(JSON.toJSONString(data));
                writer.newLine();
                writer.close();
            }
            else Log.v("ImportMessage", "ExistedFile");
        }
    }

    public void deleteBook(int position){
        Book_list.remove(position);
        dataList_updated.remove(position);
    }

    public void toggleFavorite(int position) {
        dataList_updated.get(position).Like = !dataList_updated.get(position).Like;
    }

    public void updateBookTitle(int position,String newTitle){
        dataList_updated.get(position).Title = newTitle;
    }

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
            return Objects.equals(oldItem.ID, newItem.ID);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
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
