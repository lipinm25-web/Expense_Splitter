package com.lipika.expense_splitter.controller;

import com.lipika.expense_splitter.dto.CreateGroupRequest;
import com.lipika.expense_splitter.dto.ExpenseGroupDto;
import com.lipika.expense_splitter.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseGroupDto createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request);
    }

    @GetMapping("/{groupId}")
    public ExpenseGroupDto getGroup(@PathVariable Long groupId) {
        return groupService.getGroup(groupId);
    }

    @GetMapping
    public List<ExpenseGroupDto> getAllGroups() {
        return groupService.getAllGroups();
    }
}