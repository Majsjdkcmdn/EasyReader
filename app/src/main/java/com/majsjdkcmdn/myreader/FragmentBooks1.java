package com.majsjdkcmdn.myreader;

import static android.app.Activity.RESULT_OK;

import android.Manifest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class FragmentBooks1 extends Fragment {
    private static final int REQUEST_CODE_FILE_PICKER = 1;
    private Toolbar toolbar;
    private ImageView importIcon;
    private RecyclerView recyclerViewBooks;
    private BooksManager booksManager;
    private List<Book> bookList;



    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        copyFileToPrivateDirectory(fileUri);
                    }
                }
        );
    }

    public Boolean importBooks() {
        try{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimetypes = {"application/epub","application/zip", "application/pdf", "application/ebe"};
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            filePickerLauncher.launch(intent);
            return true;}
        catch (Exception exception){
            return false;
        }
    }

    private void copyFileToPrivateDirectory(Uri fileUri) {
        Context context = requireContext();
        File privateDirectory = new File(context.getFilesDir(), "books");
        if (!privateDirectory.exists()) {
            privateDirectory.mkdirs();
        }
        File[] files = privateDirectory.listFiles();
        assert files != null;
        for(File file:files){
            Log.v("filename", file.getName());
            //TODO
        }
        File destinationFile = new File(privateDirectory, getFileName(fileUri));
        try (ParcelFileDescriptor inputPfd = context.getContentResolver().openFileDescriptor(fileUri, "r")) {
            assert inputPfd != null;
            try (FileChannel inputChannel = new FileInputStream(inputPfd.getFileDescriptor()).getChannel();
                 FileChannel outputChannel = new FileOutputStream(destinationFile).getChannel()) {
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                Log.v("succ", "succ");
            }
        } catch (IOException e) {
            Log.e("error", "shibai");
        }
        for(File file:files){
            Log.v("filename", file.getName());
            //TODO
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
                    };
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
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.fragment_books1, container, false);

            toolbar = view.findViewById(R.id.books_toolbar);
            importIcon = view.findViewById(R.id.toolbar_import);
            recyclerViewBooks = view.findViewById(R.id.books_list_all);
            recyclerViewBooks.setLayoutManager(new LinearLayoutManager(getContext()));

            booksManager = new BooksManager();
            bookList = booksManager.getBooks();
            recyclerViewBooks.setAdapter(booksManager);
            booksManager.setOnBookCoverClickListener(new BooksManager.OnBookCoverClickListener() {
                @Override
                public void onBookCoverClick(int position) {
                    //bookList.get(position).open();
                    Log.v("good", String.valueOf(position));
                }
            });

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                activity.setSupportActionBar(toolbar);
                Objects.requireNonNull(activity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
            }

            importIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (importBooks()) {
                        BooksManager.renew();
                    }
                }
            });

        return view;
    }
}