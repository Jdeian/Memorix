package com.example.memorix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlashCardAdapter extends RecyclerView.Adapter<FlashCardAdapter.FlashCardViewHolder> {

    private List<FlashCardData> flashcards;

    public FlashCardAdapter(List<FlashCardData> flashcards) {
        this.flashcards = flashcards;
    }

    @NonNull
    @Override
    public FlashCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards, parent, false);
        return new FlashCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashCardViewHolder holder, int position) {
        FlashCardData flashcard = flashcards.get(position);
        holder.termTextView.setText(flashcard.getTerm());
        holder.definitionTextView.setText(flashcard.getDefinition());

        holder.cardBack.setVisibility(View.GONE);

        holder.cardFront.setTag("front_" + position);
        holder.cardBack.setTag("back_" + position);

        holder.cardFront.setOnClickListener(v -> {
            flipCard(holder.cardFront, holder.cardBack, null);
        });

        holder.cardBack.setOnClickListener(v -> {
            flipCard(holder.cardBack, holder.cardFront, null);
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    static class FlashCardViewHolder extends RecyclerView.ViewHolder {
        TextView termTextView;
        TextView definitionTextView;
        View cardFront;
        View cardBack;

        public FlashCardViewHolder(@NonNull View itemView) {
            super(itemView);
            termTextView = itemView.findViewById(R.id.term);
            definitionTextView = itemView.findViewById(R.id.definition);
            cardFront = itemView.findViewById(R.id.card_front);
            cardBack = itemView.findViewById(R.id.card_back);
        }
    }

    public void flipCard(View front, View back, FlashCard.FlipCallback callback) {
        if (front.getVisibility() == View.VISIBLE) {
            front.animate()
                    .rotationY(90)
                    .setDuration(200)
                    .withEndAction(() -> {
                        front.setVisibility(View.GONE);
                        back.setVisibility(View.VISIBLE);
                        back.setRotationY(-90);
                        back.animate().rotationY(0).setDuration(200).withEndAction(() -> {
                            if (callback != null) {
                                callback.onFlipComplete();
                            }
                        }).start();
                    })
                    .start();
        } else {
            back.animate()
                    .rotationY(90)
                    .setDuration(200)
                    .withEndAction(() -> {
                        back.setVisibility(View.GONE);
                        front.setVisibility(View.VISIBLE);
                        front.setRotationY(-90);
                        front.animate().rotationY(0).setDuration(200).withEndAction(() -> {
                            if (callback != null) {
                                callback.onFlipComplete();
                            }
                        }).start();
                    })
                    .start();
        }
    }
}
