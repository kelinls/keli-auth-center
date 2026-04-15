package com.keli.userinfoserver.controller;

import com.keli.common.utils.R;
import com.keli.userinfoserver.dto.UserDetailResponse;
import com.keli.userinfoserver.dto.UserInfoUpsertRequest;
import com.keli.userinfoserver.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    private final UserService userService;

    public UserInfoController(UserService userService) {
        this.userService = userService;
    }



    @GetMapping("/user/{uid}")
    public R<UserDetailResponse> getByUid(@PathVariable String uid) {
        UserDetailResponse result = userService.getByUid(uid);
        if (result == null) {
            return R.error("用户不存在");
        }
        return R.success(result);
    }

    @PostMapping("/user")
    public R<UserDetailResponse> create(@RequestBody UserInfoUpsertRequest request) {
        return R.success(userService.create(request));
    }

    @PutMapping("/user/{uid}")
    public R<UserDetailResponse> update(@PathVariable String uid, @RequestBody UserInfoUpsertRequest request) {
        UserDetailResponse updated = userService.update(uid, request);
        if (updated == null) {
            return R.error("用户不存在");
        }
        return R.success(updated);
    }

    @DeleteMapping("/user/{uid}")
    public R<Void> delete(@PathVariable String uid) {
        if (!userService.delete(uid)) {
            return R.error("用户不存在");
        }
        return R.success("删除成功", null);
    }
}
