package com.example.devroad.Supabase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.devroad.Models.*;

public class SupabaseClient {

    private static final String BASE_URL = "https://ctxizhgnbkymgsjunabe.supabase.co/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImN0eGl6aGduYmt5bWdzanVuYWJlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM5MzIyMDQsImV4cCI6MjA3OTUwODIwNH0.ACA_24L6rfBTqijWDqetcLkRNPlIwMX0cgb3oJbfrvI";

    private static Retrofit retrofit = null;
    private static SupabaseClient instance;
    private final SupabaseAuthApi authApi;
    private final SupabaseDataApi dataApi;
    private String accessToken;

    private SupabaseClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("apikey", API_KEY)
                                .header("Content-Type", "application/json");

                        if (accessToken != null) {
                            requestBuilder.header("Authorization", "Bearer " + accessToken);
                        }

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authApi = retrofit.create(SupabaseAuthApi.class);
        dataApi = retrofit.create(SupabaseDataApi.class);
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public SupabaseAuthApi getAuthApi() {
        return authApi;
    }

    public SupabaseDataApi getDataApi() {
        return dataApi;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    // Auth API Interface
    public interface SupabaseAuthApi {
        @POST("/auth/v1/signup")
        Call<AuthResponse> signUp(@Body SignUpRequest request);

        @POST("/auth/v1/token?grant_type=password")
        Call<AuthResponse> signIn(@Body SignInRequest request);

        @POST("/auth/v1/logout")
        Call<Void> signOut(@Header("Authorization") String token);

        @GET("/auth/v1/user")
        Call<User> getCurrentUser(@Header("Authorization") String token);
    }

    // Data API Interface
    public interface SupabaseDataApi {
        @GET("rest/v1/users")
        Call<List<User>> getUser(@Query("id") String userId, @Query("select") String select);
        
        @GET("rest/v1/users")
        Call<List<User>> getUserById(@Query("id") String userIdFilter, @Query("select") String select);

        @POST("rest/v1/users")
        Call<User> createUser(@Body User user);

        @GET("rest/v1/cours")
        Call<List<Cours>> getAllCourses(@Query("select") String select, @Query("order") String order);

        @GET("rest/v1/lessons")
        Call<List<Lesson>> getLessonsByCourse(@Query("cours_id") String courseId, @Query("select") String select, @Query("order") String order);

        @GET("rest/v1/flashcards")
        Call<List<Flashcard>> getFlashcardsByLesson(@Query("lesson_id") String lessonId, @Query("select") String select, @Query("order") String order);

        @GET("rest/v1/exercises")
        Call<List<Exercise>> getExercisesByLesson(@Query("lesson_id") String lessonId, @Query("select") String select, @Query("order") String order);

        @GET("rest/v1/user_progress")
        Call<List<UserProgress>> getUserProgress(@Query("user_id") String userId, @Query("select") String select);

        @POST("rest/v1/user_progress")
        Call<UserProgress> createProgress(@Body UserProgress progress);

        @PATCH("rest/v1/users")
        Call<Void> updateUserScore(@Query("id") String userId, @Body UpdateScoreRequest request);
        
        @POST("rest/v1/rpc/update_user_score")
        Call<Void> updateUserScoreRPC(@Body UpdateScoreRequest request);
    }

    // Request/Response Classes
    public static class SignUpRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        @SerializedName("data")
        public Map<String, String> data;

        public SignUpRequest(String email, String password, Map<String, String> data) {
            this.email = email;
            this.password = password;
            this.data = data;
        }
    }

    public static class SignInRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        public SignInRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        @SerializedName("access_token")
        public String accessToken;

        @SerializedName("refresh_token")
        public String refreshToken;

        @SerializedName("user")
        public AuthUser user;

        @SerializedName("error")
        public String error;

        @SerializedName("error_description")
        public String errorDescription;
    }

    public static class AuthUser {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;

        @SerializedName("user_metadata")
        public Map<String, Object> userMetadata;
    }

    public static class UpdateScoreRequest {
        @SerializedName("score")
        public int score;

        public UpdateScoreRequest(int score) {
            this.score = score;
        }
    }
}
