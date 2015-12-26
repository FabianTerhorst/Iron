package io.fabianterhorst.iron.sample;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

public interface GitHubService {

    @GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);

    @GET("/users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);

}
