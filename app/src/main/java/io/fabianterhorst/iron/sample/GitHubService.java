package io.fabianterhorst.iron.sample;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface GitHubService {

    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);

    @GET("/users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);

    @GET("/users/{user}/repos")
    Observable<List<Repo>> listReposRxJava(@Path("user") String user);
}
