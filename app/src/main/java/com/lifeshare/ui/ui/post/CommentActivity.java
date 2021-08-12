package com.lifeshare.ui.ui.post;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.BaseActivity;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.CommentLikeOrLoveRequest;
import com.lifeshare.network.request.CommentRequest;
import com.lifeshare.network.request.CommentUpdateRequest;
import com.lifeshare.network.request.CreateCommentRequest;
import com.lifeshare.network.request.DeleteCommentRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.network.response.CommentResponse;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.ui.ImageFullScreenDialogFragment;
import com.lifeshare.ui.save_broadcast.ShowPreviousBroadcastAndChatActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.DateTimeHelper;

import org.jetbrains.annotations.NotNull;

public class CommentActivity extends BaseActivity {

    RecyclerView rvComment;
    CommentAdapter commentAdapter;
    AppCompatImageView ivBack, ivPostImage, ivSend;
    AppCompatTextView tvPostTitle, tvPostTime;
    ChannelArchiveResponse postData;
    AppCompatEditText etComment;
    Boolean isEdit = false;
    int channelCommentId = 0;
    LinearLayout llPost;
    private CommentResponse commentResponses = new CommentResponse();
    private int pageNo = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean isFromNotification = false;
    private String channelId;
    private String title;
    private String link;
    private String image;
    private String createdAt;
    private String video_url;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postData = getIntent().getParcelableExtra("postData");
        isFromNotification = getIntent().getBooleanExtra(Const.FROM_NOTIFICATION, false);
        if (isFromNotification) {
            channelId = getIntent().getStringExtra("channelId");
            title = getIntent().getStringExtra("title");
            channelId = getIntent().getStringExtra("channelId");
            link = getIntent().getStringExtra("link");
            image = getIntent().getStringExtra("image");
            createdAt = getIntent().getStringExtra("createdAt");
            video_url = getIntent().getStringExtra("video_url");
            type = getIntent().getStringExtra("type");
        }
        initView();
    }

    private void initView() {
        llPost = findViewById(R.id.llPost);
        ivSend = findViewById(R.id.ivSend);
        ivPostImage = findViewById(R.id.ivPostImage);
        tvPostTime = findViewById(R.id.tvPostTime);
        tvPostTitle = findViewById(R.id.tvPostTitle);
        ivBack = findViewById(R.id.ic_back);
        rvComment = findViewById(R.id.rvComment);
        etComment = findViewById(R.id.etComment);

        setData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvComment.setLayoutManager(layoutManager);
        commentAdapter = new CommentAdapter((item, type) -> {
            switch (type) {
                case "delete":
                    otherDialog(this, getResources().getString(R.string.delete_comment_text), getResources().getString(R.string.yes), getResources().getString(R.string.no), message -> {
                        if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                            deleteComment(item.getId());
                        }
                    });
                    break;
                case "edit":
                    isEdit = true;
                    channelCommentId = item.getId();
                    etComment.setText(item.getComment());
                    break;
                case "like":
                    loveLikeComment(item.getChannelId(),
                            item.getId(), "like", item.getUserLike());
                    break;
                case "love":
                    loveLikeComment(item.getChannelId(),
                            item.getId(), "love", item.getUserLove());
                    break;
            }
        });
        rvComment.setAdapter(commentAdapter);
        rvComment.setAdapter(commentAdapter);
        rvComment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= 10) {
                        pageNo++;
                        isLoading = true;
                        getCommentList(false);
                    }
                }
            }
        });
        getCommentList(false);
        ivBack.setOnClickListener(v -> onBackPressed());

        ivSend.setOnClickListener(v -> {
            if (TextUtils.isEmpty(etComment.getText().toString().trim())) {
                Toast.makeText(CommentActivity.this, getString(R.string.please_add_comment), Toast.LENGTH_SHORT).show();
            } else {
                if (isEdit && channelCommentId != 0) {
                    updateComment();
                } else {
                    createComment();
                }
            }
        });
        llPost.setOnClickListener(v -> {
            if (isFromNotification) {
                if (type.equals("1")) {
                    if (!link.trim().isEmpty()) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String url = link;
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else if (!image.isEmpty()) {
                        DialogFragment dialogFragment = ImageFullScreenDialogFragment.newInstance(image);
                        dialogFragment.show(getSupportFragmentManager(), "ImageFullScreenDialogFragment");
                    }
                } else {
                    ChannelArchiveResponse postData = null;
                    Intent intent = new Intent(this, ShowPreviousBroadcastAndChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Const.CHANNAL_DATA, postData);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            } else {
                if (postData.getType().equals("1")) {
                    if (!postData.getLink().trim().isEmpty()) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String url = postData.getLink();
                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } else if (!postData.getImage().isEmpty()) {
                        DialogFragment dialogFragment = ImageFullScreenDialogFragment.newInstance(postData.getImage());
                        dialogFragment.show(getSupportFragmentManager(), "ImageFullScreenDialogFragment");
                    }
                } else {
                    Intent intent = new Intent(this, ShowPreviousBroadcastAndChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Const.CHANNAL_DATA, postData);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            }
        });
    }

    private void setData() {
        if (isFromNotification) {
            tvPostTitle.setText(DateTimeHelper.getInstance().getDefaultDateTimeFromUtcDateTime(title));
            tvPostTime.setText(DateTimeHelper.getInstance().getTimeAgo(createdAt));
            if (type.equals("1")) {
                Glide.with(LifeShare.getInstance())
                        .load(image)
                        .apply(new RequestOptions().error(R.drawable.ic_archive).placeholder(R.drawable.ic_archive))
                        .into(ivPostImage);
                tvPostTitle.setText(title);
            } else {
                if (video_url != null && !video_url.trim().isEmpty()) {
                    Glide.with(LifeShare.getInstance())
                            .load(image)
                            .apply(new RequestOptions().error(R.drawable.ic_video_chat).placeholder(R.drawable.ic_video_chat))
                            .into(ivPostImage);

                } else {
                    Glide.with(LifeShare.getInstance())
                            .load(image)
                            .apply(new RequestOptions().error(R.drawable.ic_chat).placeholder(R.drawable.ic_chat))
                            .into(ivPostImage);

                }
            }
        } else {
            tvPostTitle.setText(DateTimeHelper.getInstance().getDefaultDateTimeFromUtcDateTime(postData.getTitle()));
            tvPostTime.setText(DateTimeHelper.getInstance().getTimeAgo(postData.getCreatedAt()));
            if (postData.getType().equals("1")) {
                Glide.with(LifeShare.getInstance())
                        .load(postData.getImage())
                        .apply(new RequestOptions().error(R.drawable.ic_archive).placeholder(R.drawable.ic_archive))
                        .into(ivPostImage);
                tvPostTitle.setText(postData.getTitle());
            } else {
                if (postData.getVideo_url() != null && !postData.getVideo_url().trim().isEmpty()) {
                    Glide.with(LifeShare.getInstance())
                            .load(postData.getImage())
                            .apply(new RequestOptions().error(R.drawable.ic_video_chat).placeholder(R.drawable.ic_video_chat))
                            .into(ivPostImage);

                } else {
                    Glide.with(LifeShare.getInstance())
                            .load(postData.getImage())
                            .apply(new RequestOptions().error(R.drawable.ic_chat).placeholder(R.drawable.ic_chat))
                            .into(ivPostImage);

                }
            }
        }
    }

    private void loveLikeComment(Integer channelId, Integer id, String type, Integer userLove) {
        int action;
        if (userLove == 1) {
            action = 0;
        } else {
            action = 1;
        }
        showLoading();
        CommentLikeOrLoveRequest request = new CommentLikeOrLoveRequest();
        request.setChannelId(channelId);
        request.setCommentId(id);
        request.setType(type);
        request.setAction(action);
        WebAPIManager.getInstance().commentLikeOrLove(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                pageNo = 0;
                getCommentList(true);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });
    }

    private void deleteComment(Integer id) {
        showLoading();
        DeleteCommentRequest request = new DeleteCommentRequest();
        request.setChannelCommentId(id);
        WebAPIManager.getInstance().deleteComment(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                pageNo = 0;
                getCommentList(true);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });
    }

    private void updateComment() {
        showLoading();
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setChannelId(channelCommentId);
        request.setComment(etComment.getText().toString().trim());
        WebAPIManager.getInstance().updateComment(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                etComment.setText("");
                channelCommentId = 0;
                hideLoading();
                pageNo = 0;
                getCommentList(true);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });
    }

    private void createComment() {
        showLoading();
        CreateCommentRequest request = new CreateCommentRequest();
        if (isFromNotification) {
            request.setChannelId(Integer.valueOf(channelId));
        } else {
            request.setChannelId(postData.getId());
        }
        request.setComment(etComment.getText().toString().trim());
        WebAPIManager.getInstance().createComment(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                etComment.setText("");
                channelCommentId = 0;
                hideLoading();
                pageNo = 0;
                getCommentList(true);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });
    }

    private void getCommentList(boolean isRefresh) {
        showLoading();
        CommentRequest request = new CommentRequest();
        if (isFromNotification) {
            request.setChannelId(Integer.valueOf(channelId));
        } else {
            request.setChannelId(postData.getId());
        }
        request.setPageNo(String.valueOf(pageNo));
        WebAPIManager.getInstance().getCommentList(request, new RemoteCallback<CommentResponse>() {
            @Override
            public void onSuccess(CommentResponse response) {
                commentResponses = response;
                commentAdapter.addItems(commentResponses.getData(), isRefresh);
                if (commentResponses.getData().isEmpty()) {
                    isLastPage = true;
                } else {
                    isLastPage = false;
                }
                isLoading = false;
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });

    }
}