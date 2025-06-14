package com.majsjdkcmdn.myreader;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FragmentBooks extends Fragment {
    private RecyclerView recyclerViewBooks;
    private BooksManager booksManager;
    private List<Book> bookList;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    Context context;
    Resources res;
    File booksDirectory;
    File assetsDirectory;
    File database;
    String name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        copyFileToPrivateDirectory(fileUri);
                        try {
                            booksManager.importBook(booksDirectory+"/"+name);
                            booksManager.notifyItemRangeChanged(0, bookList.size());
                        } catch (IOException e) {
                            Log.e("error", String.valueOf(e));
                        }
                    }
                }
        );
    }

    public void importBooks() {
        try{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimetypes = {"application/epub+zip", "application/pdf", "application/ebe"};
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            filePickerLauncher.launch(intent);}
        catch (Exception e){
            Log.e("error", String.valueOf(e));
        }
    }

    public void modifyName(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("重命名");
        final EditText input = new EditText(requireContext());
        input.setText(bookList.get(position).Title);
        builder.setView(input);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTitle = input.getText().toString();
                if (!newTitle.isEmpty()) {
                    bookList.get(position).Title = newTitle;
                    booksManager.updateBookTitle(position, newTitle);
                    booksManager.notifyItemChanged(position, 0);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void copyFileToPrivateDirectory(Uri fileUri) {
        File destinationFile = new File(booksDirectory, getFileName(fileUri));
        Log.v("filename", destinationFile.getName());
        try (ParcelFileDescriptor inputPfd = context.getContentResolver().openFileDescriptor(fileUri, "r")) {
            assert inputPfd != null;
            try (FileChannel inputChannel = new FileInputStream(inputPfd.getFileDescriptor()).getChannel();
                 FileChannel outputChannel = new FileOutputStream(destinationFile).getChannel()) {
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                Log.v("Success", "copy success!");
            }
        } catch (IOException e) {
            Log.e("error", String.valueOf(e));
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        name = result;
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.fragment_books, container, false);
            context = requireContext();
            res = getResources();
            booksDirectory = new File(context.getFilesDir(), "books");
            assetsDirectory = new File(context.getFilesDir(), "assets");
            database = new File(context.getFilesDir(), "database.json");
            if(!booksDirectory.exists()){
                booksDirectory.mkdir();
            }
            if(!assetsDirectory.exists()){
                assetsDirectory.mkdir();
            }
            if(!database.exists()){
                try {
                    database.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Log.v("File", String.valueOf(database));
                Log.v("Create","Creating database");
            }

            //debug
            File[] files = context.getFilesDir().listFiles();
            assert files != null;
            for(File file:files){
                Log.v("filename", file.getName());
            }
            for(File file: Objects.requireNonNull(assetsDirectory.listFiles())){
                Log.v("filename", file.getName());
            }
            for(File file: Objects.requireNonNull(booksDirectory.listFiles())){
                Log.v("filename", file.getName());
            }
            //debug
            //TODO REMOVE

            Toolbar toolbar = view.findViewById(R.id.books_toolbar);
            ImageView importIcon = view.findViewById(R.id.toolbar_import);
            recyclerViewBooks = view.findViewById(R.id.books_list_all);
            recyclerViewBooks.setLayoutManager(new LinearLayoutManager(getContext()));

            try {
                booksManager = new BooksManager(res, booksDirectory, database);
            } catch (IOException e) {
                try {
                    booksManager = new BooksManager(res, database);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            bookList = booksManager.Book_list;
            recyclerViewBooks.setAdapter(booksManager);

            importIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    importBooks();
                }
            });
            booksManager.setOnBookCoverClickListener(new BooksManager.OnBookCoverClickListener() {
                @Override
                public void onBookCoverClick(int position) {
                    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics dm = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(dm);
                    int width = dm.widthPixels;         // 屏幕宽度（像素）
                    int height = dm.heightPixels;       // 屏幕高度（像素）
                    float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
                    int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
                    int screenHeight = (int) (height / density);// 屏幕高度(dp)
                    Log.v("open", String.valueOf(position));
                    Intent intent = new Intent(getActivity(), ReadPage.class);
                    intent.putExtra("ReadingPath", bookList.get(position).ReadingPath);
                    intent.putExtra("bookPage", bookList.get(position).LastPage);
                    intent.putExtra("bookTitle", bookList.get(position).Title);
                    intent.putStringArrayListExtra("ChapterList", (ArrayList<String>) bookList.get(position).ChapterSeq);
                    intent.putExtra("height", screenHeight-100);
                    intent.putExtra("width", screenWidth-36);
                    intent.putExtra("density", density);
                    startActivity(intent);
                }
            });
            booksManager.setOnBookModifyClickListener(new BooksManager.OnBookModifyClickListener() {
                @Override
                public void onBookModifyClick(int position) {
                    modifyName(position);
                }
            });
            booksManager.setOnBookLikeClickListener(new BooksManager.OnBookLikeClickListener() {
                @Override
                public void onBookLikeClick(int position) {
                    bookList.get(position).Like = !bookList.get(position).Like;
                    booksManager.toggleFavorite(position);
                    booksManager.notifyItemChanged(position, 0);
                    Log.v("like", String.valueOf(position));
                }
            });
            booksManager.setOnBookDeleteClickListener(new BooksManager.OnBookDeleteClickListener() {
                @Override
                public void onBookDeleteClick(int position){
                    Log.v("delete", String.valueOf(position));
                    File bookfile = new File(bookList.get(position).FilePath);
                    File assetsfile = new File(bookList.get(position).AssetsPath);
                    bookfile.delete();
                    assetsfile.delete();
                    booksManager.deleteBook(position);
                    booksManager.notifyItemRemoved(position);
                }
            });

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                activity.setSupportActionBar(toolbar);
                Objects.requireNonNull(activity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
            }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    public void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData();
    }

    private void saveData() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(database));
            for(BooksManager.DatabaseData data:booksManager.dataList_updated){
                writer.write(JSON.toJSONString(data));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}