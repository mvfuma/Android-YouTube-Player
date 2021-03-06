package com.pierfrancescosoffritti.youtubeplayer.ui;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pierfrancescosoffritti.youtubeplayer.R;
import com.pierfrancescosoffritti.youtubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.youtubeplayer.ui.menu.YouTubePlayerMenu;
import com.pierfrancescosoffritti.youtubeplayer.ui.menu.defaultMenu.DefaultYouTubePlayerMenu;
import com.pierfrancescosoffritti.youtubeplayer.utils.Utils;

public class DefaultPlayerUIController implements PlayerUIController, View.OnClickListener, YouTubePlayerFullScreenListener, YouTubePlayerListener, SeekBar.OnSeekBarChangeListener {
    @NonNull private final YouTubePlayerView youTubePlayerView;
    @NonNull private final YouTubePlayer youTubePlayer;

    @NonNull private YouTubePlayerMenu youTubePlayerMenu;

    // view responsible for intercepting clicks. Could have used controlsRoot view, but in this way I'm able to hide all the control at once by hiding controlsRoot
    @NonNull private final View panel;

    // view containing the controls
    @NonNull private final View controlsRoot;

    @NonNull private final TextView videoTitle;
    @NonNull private final TextView videoCurrentTime;
    @NonNull private final TextView videoDuration;
    @NonNull private final TextView liveVideoIndicator;

    @NonNull private final ProgressBar progressBar;
    @NonNull private final ImageView menuButton;
    @NonNull private final ImageView playPauseButton;
    @NonNull private final ImageView youTubeButton;
    @NonNull private final ImageView fullScreenButton;

    @NonNull private final ImageView customActionLeft;
    @NonNull private final ImageView customActionRight;

    @NonNull private final SeekBar seekBar;

    @Nullable private View.OnClickListener onFullScreenButtonListener;
    @Nullable private View.OnClickListener onMenuButtonClickListener;

    // view state
    private boolean isPlaying = false;
    private boolean isVisible = true;
    private boolean canFadeControls = false;

    private boolean showUI = true;
    private boolean showPlayPauseButton = true;

    public DefaultPlayerUIController(@NonNull YouTubePlayerView youTubePlayerView, @NonNull YouTubePlayer youTubePlayer, @NonNull View controlsView) {
        this.youTubePlayerView = youTubePlayerView;
        this.youTubePlayer = youTubePlayer;

        youTubePlayerMenu = new DefaultYouTubePlayerMenu(youTubePlayerView.getContext());

        panel = controlsView.findViewById(R.id.panel);

        controlsRoot = controlsView.findViewById(R.id.controls_root);

        videoTitle = controlsView.findViewById(R.id.video_title);
        videoCurrentTime = controlsView.findViewById(R.id.video_current_time);
        videoDuration = controlsView.findViewById(R.id.video_duration);
        liveVideoIndicator = controlsView.findViewById(R.id.live_video_indicator);

        progressBar = controlsView.findViewById(R.id.progress);
        menuButton = controlsView.findViewById(R.id.menu_button);
        playPauseButton = controlsView.findViewById(R.id.play_pause_button);
        youTubeButton = controlsView.findViewById(R.id.youtube_button);
        fullScreenButton = controlsView.findViewById(R.id.fullscreen_button);

        customActionLeft = controlsView.findViewById(R.id.custom_action_left_button);
        customActionRight = controlsView.findViewById(R.id.custom_action_right_button);

        seekBar = controlsView.findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(this);
        panel.setOnClickListener(this);
        playPauseButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        fullScreenButton.setOnClickListener(this);
    }

    @Override
    public void showVideoTitle(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        videoTitle.setVisibility(visibility);
    }

    @Override
    public void setVideoTitle(@NonNull String title) {
        videoTitle.setText(title);
    }

    @Override
    public void showUI(boolean show) {
        int visibility = show ? View.VISIBLE : View.INVISIBLE;
        controlsRoot.setVisibility(visibility);

        showUI = show;
    }

    @Override
    public void showPlayPauseButton(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        playPauseButton.setVisibility(visibility);

        showPlayPauseButton = show;
    }

    @Override
    public void enableLiveVideoUI(boolean enable) {
        if(enable) {
            videoDuration.setVisibility(View.INVISIBLE);
            seekBar.setVisibility(View.INVISIBLE);
            videoCurrentTime.setVisibility(View.INVISIBLE);

            liveVideoIndicator.setVisibility(View.VISIBLE);
        } else {
            videoDuration.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            videoCurrentTime.setVisibility(View.VISIBLE);

            liveVideoIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * Set custom action to the left of the Play/Pause button
     */
    @Override
    public void setCustomAction1(@NonNull Drawable icon, View.OnClickListener clickListener) {
        customActionLeft.setImageDrawable(icon);
        customActionLeft.setOnClickListener(clickListener);
        showCustomAction1(clickListener != null);
    }

    /**
     * Set custom action to the right of the Play/Pause button
     */
    @Override
    public void setCustomAction2(@NonNull Drawable icon, View.OnClickListener clickListener) {
        customActionRight.setImageDrawable(icon);
        customActionRight.setOnClickListener(clickListener);
        showCustomAction2(clickListener != null);
    }

    public void showCustomAction1(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        customActionLeft.setVisibility(visibility);
    }

    public void showCustomAction2(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        customActionRight.setVisibility(visibility);
    }

    @Override
    public void showMenuButton(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        menuButton.setVisibility(visibility);
    }

    @Override
    public void setCustomMenuButtonClickListener(@NonNull View.OnClickListener customMenuButtonClickListener) {
        this.onMenuButtonClickListener = customMenuButtonClickListener;
    }

    @NonNull
    @Override
    public YouTubePlayerMenu getMenu() {
        return youTubePlayerMenu;
    }

    @Override
    public void setMenu(@NonNull YouTubePlayerMenu youTubePlayerMenu) {
        this.youTubePlayerMenu = youTubePlayerMenu;
    }

    @Override
    public void showFullscreenButton(boolean show) {
        int visibility = show ? View.VISIBLE : View.INVISIBLE;
        fullScreenButton.setVisibility(visibility);
    }

    @Override
    public void setCustomFullScreenButtonClickListener(@NonNull View.OnClickListener customFullScreenButtonClickListener) {
        this.onFullScreenButtonListener = customFullScreenButtonClickListener;
    }

    @Override
    public void onClick(View view) {
        if(view == panel)
            toggleControlsVisibility();
        else if(view == playPauseButton)
            onPlayButtonPressed();
        else if(view == fullScreenButton)
            onFullScreenButtonPressed();
        else if(view == menuButton)
            onMenuButtonPressed();
    }

    private void onMenuButtonPressed() {
        if(onMenuButtonClickListener == null)
            youTubePlayerMenu.show(menuButton);
        else
            onMenuButtonClickListener.onClick(menuButton);
    }

    private void onFullScreenButtonPressed() {
        if(onFullScreenButtonListener == null)
            youTubePlayerView.toggleFullScreen();
        else
            onFullScreenButtonListener.onClick(fullScreenButton);
    }

    private void onPlayButtonPressed() {
        if(isPlaying)
            youTubePlayer.pause();
        else
            youTubePlayer.play();
    }

    private void updatePlayPauseButtonIcon(boolean playing) {
        int img = playing ? R.drawable.ic_pause_36dp : R.drawable.ic_play_36dp;
        playPauseButton.setImageResource(img);
    }

    private void toggleControlsVisibility() {
        final float finalAlpha = isVisible ? 0f : 1f;
        fadeControls(finalAlpha);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable fadeOutRunnable = new Runnable() {
        @Override
        public void run() {
            fadeControls(0f);
        }
    };

    private void fadeControls(final float finalAlpha) {
        if(!canFadeControls || !showUI)
            return;

        isVisible = finalAlpha != 0f;

        // if the controls are shown and the player is playing they should automatically hide after a while.
        // if the controls are hidden remove fade out runnable
        if(finalAlpha == 1f && isPlaying)
            startFadeOutViewTimer();
        else
            handler.removeCallbacks(fadeOutRunnable);


        controlsRoot.animate()
                .alpha(finalAlpha)
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        if (finalAlpha == 1f)
                            controlsRoot.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (finalAlpha == 0f)
                            controlsRoot.setVisibility(View.GONE);
                    }

                    @Override public void onAnimationCancel(Animator animator) { }
                    @Override public void onAnimationRepeat(Animator animator) { }
                }).start();
    }

    private void startFadeOutViewTimer() {
        handler.postDelayed(fadeOutRunnable, 3000);
    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        fullScreenButton.setImageResource(R.drawable.ic_fullscreen_exit_24dp);
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        fullScreenButton.setImageResource(R.drawable.ic_fullscreen_24dp);
    }

    // YouTubePlayer callbacks

    // TODO refactor this method
    @Override
    public void onStateChange(@PlayerConstants.PlayerState.State int state) {
        newSeekBarProgress = -1;

        updateControlsState(state);

        if(state == PlayerConstants.PlayerState.PLAYING || state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED) {
            panel.setBackgroundColor(ContextCompat.getColor(youTubePlayerView.getContext(), android.R.color.transparent));
            progressBar.setVisibility(View.GONE);

            if(showPlayPauseButton) playPauseButton.setVisibility(View.VISIBLE);


            canFadeControls = true;
            boolean playing = state == PlayerConstants.PlayerState.PLAYING;
            updatePlayPauseButtonIcon(playing);

            if(playing)
                startFadeOutViewTimer();
            else
                handler.removeCallbacks(fadeOutRunnable);

        } else {
            updatePlayPauseButtonIcon(false);
            fadeControls(1f);

            if(state == PlayerConstants.PlayerState.BUFFERING) {
                panel.setBackgroundColor(ContextCompat.getColor(youTubePlayerView.getContext(), android.R.color.transparent));
                if(showPlayPauseButton) playPauseButton.setVisibility(View.INVISIBLE);

                customActionLeft.setVisibility(View.GONE);
                customActionRight.setVisibility(View.GONE);

                canFadeControls = false;
            }

            if(state == PlayerConstants.PlayerState.UNSTARTED) {
                canFadeControls = false;

                progressBar.setVisibility(View.GONE);
                if(showPlayPauseButton) playPauseButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateControlsState(int state) {
        switch (state) {
            case PlayerConstants.PlayerState.ENDED:
                isPlaying = false;
                break;
            case PlayerConstants.PlayerState.PAUSED:
                isPlaying = false;
                break;
            case PlayerConstants.PlayerState.PLAYING:
                isPlaying = true;
                break;
            case PlayerConstants.PlayerState.UNSTARTED:
                resetUI();
                break;
            default:
                break;
        }


        updatePlayPauseButtonIcon(!isPlaying);
    }

    @Override
    public void onCurrentSecond(float second) {
        // ignore if the user is currently moving the SeekBar
        if(seekBarTouchStarted)
            return;
        // ignore if the current time is older than what the user selected with the SeekBar
        if(newSeekBarProgress > 0 && !Utils.formatTime(second).equals(Utils.formatTime(newSeekBarProgress)))
            return;

        newSeekBarProgress = -1;

        seekBar.setProgress((int) second);
    }

    @Override
    public void onVideoDuration(float duration) {
        videoDuration.setText(Utils.formatTime(duration));
        seekBar.setMax((int) duration);
    }

    @Override
    public void onVideoId(final String videoId) {
        youTubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoId));
                controlsRoot.getContext().startActivity(intent);
            }
        });
    }

    @Override public void onReady() { }
    @Override public void onMessage(String log) { }
    @Override public void onPlaybackQualityChange(@PlayerConstants.PlaybackQuality.Quality String playbackQuality) { }
    @Override public void onPlaybackRateChange(@PlayerConstants.PlaybackRate.Rate String rate) { }
    @Override public void onError(@PlayerConstants.PlayerError.Error int error) { }
    @Override public void onApiChange() { }

    // SeekBar callbacks

    private boolean seekBarTouchStarted = false;
    // I need this variable because onCurrentSecond gets called every 100 mils, so without the proper checks on this variable in onCurrentSeconds the seek bar glitches when touched.
    private int newSeekBarProgress = -1;

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        videoCurrentTime.setText(Utils.formatTime(i));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekBarTouchStarted = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(isPlaying)
            newSeekBarProgress = seekBar.getProgress();

        youTubePlayer.seekTo(seekBar.getProgress());
        seekBarTouchStarted = false;
    }

    private void resetUI() {
        seekBar.setProgress(0);
        seekBar.setMax(0);
        videoDuration.post(new Runnable() {
            @Override
            public void run() {
                videoDuration.setText("");
            }
        });
        youTubeButton.setOnClickListener(null);
    }
}
