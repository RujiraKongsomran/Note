package com.rujira.note;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";

    private EditText etTitle;
    private EditText etDescription;
    private EditText etPriority;
    private TextView tvData;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private DocumentReference noteRef = db.document("Notebook/My First Note");
    private ListenerRegistration noteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInstances();
    }

    private void initInstances() {
        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        etPriority = (EditText) findViewById(R.id.etPriority);
        tvData = (TextView) findViewById(R.id.tvData);

    }

    @Override
    protected void onStart() {
        super.onStart();
        notebookRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                String data = "";
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Note note = documentSnapshot.toObject(Note.class);
                    note.setDocumentId(documentSnapshot.getId());

                    String documentId = note.getDocumentId();
                    String title = note.getTitle();
                    String description = note.getDescription();
                    int priority = note.getPriority();

                    data += "ID : " + documentId +
                            "\nTitle : " + title +
                            "\nDescription : " + description +
                            "\nPriority : " + priority +
                            "\n" + "------------------------------------------\n\n";
                }

                tvData.setText(data);
            }
        });
    }

    public void addNote(View v) {
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();

        if (etPriority.length() == 0) {
            etPriority.setText("0");
        }
        int priority = Integer.parseInt(etPriority.getText().toString());

        Note note = new Note(title, description, priority);
        notebookRef.add(note);
    }

    public void updateDescription(View v) {
        String description = etDescription.getText().toString();

//        Map<String, Object> note = new HashMap<>();
//        note.put(KEY_DESCRIPTION, description);

//        noteRef.set(note, SetOptions.merge());
        noteRef.update(KEY_DESCRIPTION, description);
    }

    public void deleteDescription(View v) {
//        Map<String, Object> note = new HashMap<>();
//        note.put(KEY_DESCRIPTION, FieldValue.delete());
//
//        noteRef.update(note);
        noteRef.update(KEY_DESCRIPTION, FieldValue.delete());
    }

    public void deleteNote(View v) {
        noteRef.delete();
    }

    public void loadNotes(View v) {
        Task task1 = notebookRef.whereLessThan("priority", 2)
                .orderBy("priority")
                .get();

        Task task2 = notebookRef.whereGreaterThan("priority", 2)
                .orderBy("priority")
                .get();

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(task1, task2);
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                String data = "";
                for (QuerySnapshot queryDocumentSnapshots : querySnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Note note = documentSnapshot.toObject(Note.class);
                        note.setDocumentId(documentSnapshot.getId());

                        String documentId = note.getDocumentId();
                        String title = note.getTitle();
                        String description = note.getDescription();
                        int priority = note.getPriority();

                        data += "ID : " + documentId +
                                "\nTitle : " + title +
                                "\nDescription : " + description +
                                "\nPriority : " + priority +
                                "\n" + "------------------------------------------\n\n";
                    }
                }

                tvData.setText(data);

            }
        });

    }

}
