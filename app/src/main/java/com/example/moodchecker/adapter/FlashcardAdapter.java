    package com.example.moodchecker.adapter;

    import android.media.Image;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ImageButton;
    import android.widget.LinearLayout;
    import android.widget.RadioButton;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;
    import com.example.moodchecker.R;
    import com.example.moodchecker.model.Flashcard;
    import com.google.firebase.firestore.FirebaseFirestore;

    import java.util.List;

    public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

        private List<Flashcard> flashcards;
        private FirebaseFirestore firestore;
        private String userId;
        private String reviewerId;

        public FlashcardAdapter(List<Flashcard> flashcards) {
            this.flashcards = flashcards;
        }

        @NonNull
        @Override
        public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashcard_item, parent, false);
            return new FlashcardViewHolder(view);
        }
        @Override
        public void onBindViewHolder(FlashcardViewHolder holder, int position) {
            Flashcard flashcard = flashcards.get(position);
            holder.questionTextView.setText(flashcard.getQuestion());

            // Display the answer for short text type questions
            if (flashcard.getAnswerType().equals("Short Text")) {
                holder.answerTextView.setVisibility(View.VISIBLE); // Make answer visible
                holder.answerTextView.setText(flashcard.getAnswer()); // Set the answer text
            } else {
                holder.answerTextView.setVisibility(View.GONE); // Hide answer for multiple choice questions
            }

            if (flashcard.getAnswerType() != null && flashcard.getAnswerType().equals("Multiple Choice")) {
                // Set the options and display the radio buttons
                holder.optionsContainer.setVisibility(View.VISIBLE);
                List<String> options = flashcard.getOptions();

                // Check if there are enough options to set text for all RadioButtons
                if (options.size() > 0) holder.optionOne.setText(options.get(0));
                if (options.size() > 1) holder.optionTwo.setText(options.get(1));
                if (options.size() > 2) holder.optionThree.setText(options.get(2));
                if (options.size() > 3) holder.optionFour.setText(options.get(3));

                // Set up a listener to determine the selected option
                holder.optionOne.setOnClickListener(view -> {
                    flashcard.setCorrectAnswer(holder.optionOne.getText().toString());
                    Toast.makeText(view.getContext(), "Correct answer: " + flashcard.getCorrectAnswer(), Toast.LENGTH_SHORT).show();
                });

                holder.optionTwo.setOnClickListener(view -> {
                    flashcard.setCorrectAnswer(holder.optionTwo.getText().toString());
                    Toast.makeText(view.getContext(), "Correct answer: " + flashcard.getCorrectAnswer(), Toast.LENGTH_SHORT).show();
                });

                holder.optionThree.setOnClickListener(view -> {
                    flashcard.setCorrectAnswer(holder.optionThree.getText().toString());
                    Toast.makeText(view.getContext(), "Correct answer: " + flashcard.getCorrectAnswer(), Toast.LENGTH_SHORT).show();
                });

                holder.optionFour.setOnClickListener(view -> {
                    flashcard.setCorrectAnswer(holder.optionFour.getText().toString());
                    Toast.makeText(view.getContext(), "Correct answer: " + flashcard.getCorrectAnswer(), Toast.LENGTH_SHORT).show();
                });

            } else {
                holder.optionsContainer.setVisibility(View.GONE);
            }

            holder.deleteButton.setOnClickListener(view -> {
                // Callback to delete the flashcard
                deleteFlashcard(flashcard, position);
            });
        }

        @Override
        public int getItemCount() {
            return flashcards.size();
        }

        // Callback interface for deletion
        public interface FlashcardDeleteCallback {
            void onDelete(Flashcard flashcard, int position);
        }

        private FlashcardDeleteCallback deleteCallback;

        public void setDeleteCallback(FlashcardDeleteCallback deleteCallback) {
            this.deleteCallback = deleteCallback;
        }

        private void deleteFlashcard(Flashcard flashcard, int position) {
            if (deleteCallback != null) {
                deleteCallback.onDelete(flashcard, position);
            }
        }

        public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
            TextView questionTextView, answerTextView;
            ImageButton deleteButton;
            LinearLayout optionsContainer;
            RadioButton optionOne, optionTwo, optionThree, optionFour;

            public FlashcardViewHolder(@NonNull View itemView) {
                super(itemView);
                questionTextView = itemView.findViewById(R.id.questionTextView);
                answerTextView = itemView.findViewById(R.id.answerTextView);
                deleteButton = itemView.findViewById(R.id.deleteButton);

                optionsContainer = itemView.findViewById(R.id.optionsContainer);
                optionOne = itemView.findViewById(R.id.optionOne);
                optionTwo = itemView.findViewById(R.id.optionTwo);
                optionThree = itemView.findViewById(R.id.optionThree);
                optionFour = itemView.findViewById(R.id.optionFour);
            }
        }
    }
