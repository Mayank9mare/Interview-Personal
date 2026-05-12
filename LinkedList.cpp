#include <bits/stdc++.h>
using namespace std;

// ---------------------------------------------------------------------------
// Linked List Problems Reference - C++
// Covers: build/print | reverse | middle | merge | cycle | nth from end |
//         palindrome | add numbers | intersection | reorder | sort
// ---------------------------------------------------------------------------

struct ListNode {
    int val;
    ListNode* next;
    ListNode(int v = 0) : val(v), next(nullptr) {}
};

// Build a singly linked list from an array.
ListNode* buildList(const vector<int>& a) {
    ListNode dummy;
    ListNode* tail = &dummy;
    for (int x : a) {
        tail->next = new ListNode(x);
        tail = tail->next;
    }
    return dummy.next;
}

// Print list values in order.
void printList(ListNode* head) {
    for (ListNode* cur = head; cur; cur = cur->next) {
        cout << cur->val;
        if (cur->next) cout << " -> ";
    }
    cout << endl;
}

int length(ListNode* head) {
    int n = 0;
    while (head) {
        n++;
        head = head->next;
    }
    return n;
}


// ---------------------------------------------------------------------------
// 1. Insert / Delete
//    PROBLEM: Add nodes at the head/tail and remove the first node with a
//             target value while preserving the rest of the list.
//    MENTAL MODEL: "use a dummy node when the head itself may change"
// ---------------------------------------------------------------------------
ListNode* insertAtHead(ListNode* head, int val) {
    ListNode* node = new ListNode(val);
    node->next = head;
    return node;
}

ListNode* insertAtTail(ListNode* head, int val) {
    ListNode* node = new ListNode(val);
    if (!head) return node;

    ListNode* cur = head;
    while (cur->next) cur = cur->next;
    cur->next = node;
    return head;
}

ListNode* deleteValue(ListNode* head, int val) {
    ListNode dummy;
    dummy.next = head;
    ListNode* prev = &dummy;

    while (prev->next) {
        if (prev->next->val == val) {
            ListNode* victim = prev->next;
            prev->next = victim->next;
            delete victim;
            break;  // delete only the first match
        }
        prev = prev->next;
    }
    return dummy.next;
}


// ---------------------------------------------------------------------------
// 2. Reverse Linked List
//    PROBLEM: Reverse all links in a singly linked list and return the new head.
//    MENTAL MODEL: "walk forward, flipping each pointer to point backward"
// ---------------------------------------------------------------------------
ListNode* reverseList(ListNode* head) {
    ListNode* prev = nullptr;
    ListNode* cur = head;

    while (cur) {
        ListNode* nextNode = cur->next;  // save the rest before overwriting
        cur->next = prev;                // flip current node backward
        prev = cur;                      // advance prev and cur
        cur = nextNode;
    }
    return prev;                         // prev is the new head
}

ListNode* reverseBetween(ListNode* head, int left, int right) {
    if (!head || left == right) return head;

    ListNode dummy;
    dummy.next = head;
    ListNode* before = &dummy;

    // Move before to the node just before the segment to reverse.
    for (int i = 1; i < left; i++) before = before->next;

    ListNode* segmentTail = before->next;
    ListNode* cur = segmentTail->next;

    // Head-insertion inside the segment:
    // repeatedly remove cur and place it immediately after before.
    for (int i = 0; i < right - left; i++) {
        segmentTail->next = cur->next;
        cur->next = before->next;
        before->next = cur;
        cur = segmentTail->next;
    }

    return dummy.next;
}


// ---------------------------------------------------------------------------
// 3. Middle Node
//    PROBLEM: Return the middle node of a linked list; for even length, return
//             the second middle node.
//    MENTAL MODEL: "slow moves 1 step, fast moves 2 steps"
//    When fast reaches the end, slow is in the middle.
// ---------------------------------------------------------------------------
ListNode* middleNode(ListNode* head) {
    ListNode* slow = head;
    ListNode* fast = head;

    while (fast && fast->next) {
        slow = slow->next;
        fast = fast->next->next;
    }
    return slow;  // for even length, returns the second middle
}


// ---------------------------------------------------------------------------
// 4. Merge Two Sorted Lists
//    PROBLEM: Given two sorted linked lists, merge them into one sorted list.
//    MENTAL MODEL: "always attach the smaller front node to a result tail"
// ---------------------------------------------------------------------------
ListNode* mergeTwoLists(ListNode* a, ListNode* b) {
    ListNode dummy;
    ListNode* tail = &dummy;

    while (a && b) {
        if (a->val <= b->val) {
            tail->next = a;
            a = a->next;
        } else {
            tail->next = b;
            b = b->next;
        }
        tail = tail->next;
    }

    tail->next = a ? a : b;
    return dummy.next;
}


// ---------------------------------------------------------------------------
// 5. Cycle Detection
//    PROBLEM: Detect whether a linked list has a cycle, and if needed return
//             the node where the cycle begins.
//    MENTAL MODEL: "if fast laps slow, there is a cycle"
//    To find cycle start: reset one pointer to head, move both 1 step.
// ---------------------------------------------------------------------------
bool hasCycle(ListNode* head) {
    ListNode* slow = head;
    ListNode* fast = head;

    while (fast && fast->next) {
        slow = slow->next;
        fast = fast->next->next;
        if (slow == fast) return true;
    }
    return false;
}

ListNode* detectCycleStart(ListNode* head) {
    ListNode* slow = head;
    ListNode* fast = head;

    while (fast && fast->next) {
        slow = slow->next;
        fast = fast->next->next;
        if (slow == fast) {
            ListNode* p = head;
            while (p != slow) {
                p = p->next;
                slow = slow->next;
            }
            return p;
        }
    }
    return nullptr;
}


// ---------------------------------------------------------------------------
// 6. Remove Nth Node From End
//    PROBLEM: Delete the nth node from the end of the list in one pass.
//    MENTAL MODEL: "keep a gap of n nodes between fast and slow"
// ---------------------------------------------------------------------------
ListNode* removeNthFromEnd(ListNode* head, int n) {
    ListNode dummy;
    dummy.next = head;
    ListNode* slow = &dummy;
    ListNode* fast = &dummy;

    for (int i = 0; i < n; i++) fast = fast->next;

    while (fast->next) {
        slow = slow->next;
        fast = fast->next;
    }

    ListNode* victim = slow->next;
    slow->next = victim->next;
    delete victim;
    return dummy.next;
}


// ---------------------------------------------------------------------------
// 7. Palindrome Linked List
//    PROBLEM: Check whether the list reads the same forward and backward.
//    MENTAL MODEL: "reverse the second half, then compare both halves"
// ---------------------------------------------------------------------------
bool isPalindrome(ListNode* head) {
    if (!head || !head->next) return true;

    ListNode* mid = middleNode(head);
    ListNode* second = reverseList(mid);
    ListNode* copySecond = second;

    ListNode* first = head;
    bool ok = true;
    while (second) {
        if (first->val != second->val) {
            ok = false;
            break;
        }
        first = first->next;
        second = second->next;
    }

    reverseList(copySecond);  // restore the list shape after checking
    return ok;
}


// ---------------------------------------------------------------------------
// 8. Add Two Numbers
//    PROBLEM: Add two non-negative integers represented by reversed digit lists
//             and return the sum as a reversed digit list.
//    MENTAL MODEL: "same as grade-school addition with carry"
//    Digits are stored in reverse order: 342 is represented as 2 -> 4 -> 3.
// ---------------------------------------------------------------------------
ListNode* addTwoNumbers(ListNode* a, ListNode* b) {
    ListNode dummy;
    ListNode* tail = &dummy;
    int carry = 0;

    while (a || b || carry) {
        int sum = carry;
        if (a) { sum += a->val; a = a->next; }
        if (b) { sum += b->val; b = b->next; }

        tail->next = new ListNode(sum % 10);
        tail = tail->next;
        carry = sum / 10;
    }

    return dummy.next;
}


// ---------------------------------------------------------------------------
// 9. Intersection of Two Linked Lists
//    PROBLEM: Return the first shared node where two singly linked lists
//             intersect, or nullptr if they never meet.
//    MENTAL MODEL: "two pointers switch heads to equalize path lengths"
// ---------------------------------------------------------------------------
ListNode* getIntersectionNode(ListNode* a, ListNode* b) {
    ListNode* p = a;
    ListNode* q = b;

    while (p != q) {
        p = p ? p->next : b;
        q = q ? q->next : a;
    }
    return p;  // either the intersection node or nullptr
}


// ---------------------------------------------------------------------------
// 10. Reorder List
//     PROBLEM: Reorder L0 -> L1 -> ... -> Ln into L0 -> Ln -> L1 -> Ln-1 ...
//              in-place without changing node values.
//     MENTAL MODEL: "split, reverse second half, weave alternating nodes"
//     Example: 1 -> 2 -> 3 -> 4 -> 5 becomes 1 -> 5 -> 2 -> 4 -> 3.
// ---------------------------------------------------------------------------
void reorderList(ListNode* head) {
    if (!head || !head->next) return;

    ListNode* slow = head;
    ListNode* fast = head;
    while (fast->next && fast->next->next) {
        slow = slow->next;
        fast = fast->next->next;
    }

    ListNode* second = reverseList(slow->next);
    slow->next = nullptr;  // split the list into two halves

    ListNode* first = head;
    while (second) {
        ListNode* firstNext = first->next;
        ListNode* secondNext = second->next;

        first->next = second;
        second->next = firstNext;

        first = firstNext;
        second = secondNext;
    }
}


// ---------------------------------------------------------------------------
// 11. Sort List
//     PROBLEM: Sort a linked list in ascending order using O(n log n) time.
//     MENTAL MODEL: "merge sort fits linked lists because merging is pointer-only"
//     Time: O(n log n), extra space: O(log n) recursion stack.
// ---------------------------------------------------------------------------
ListNode* sortList(ListNode* head) {
    if (!head || !head->next) return head;

    ListNode* slow = head;
    ListNode* fast = head->next;
    while (fast && fast->next) {
        slow = slow->next;
        fast = fast->next->next;
    }

    ListNode* right = slow->next;
    slow->next = nullptr;
    ListNode* left = sortList(head);
    right = sortList(right);

    return mergeTwoLists(left, right);
}


int main() {
    cout << "=== Build / Insert / Delete ===" << endl;
    ListNode* list = buildList({1,2,3});
    list = insertAtHead(list, 0);
    list = insertAtTail(list, 4);
    list = deleteValue(list, 2);
    printList(list);  // 0 -> 1 -> 3 -> 4

    cout << "\n=== Reverse ===" << endl;
    ListNode* rev = reverseList(buildList({1,2,3,4}));
    printList(rev);  // 4 -> 3 -> 2 -> 1

    ListNode* partial = reverseBetween(buildList({1,2,3,4,5}), 2, 4);
    printList(partial);  // 1 -> 4 -> 3 -> 2 -> 5

    cout << "\n=== Middle / Merge ===" << endl;
    ListNode* midList = buildList({1,2,3,4,5});
    cout << "Middle: " << middleNode(midList)->val << endl;  // 3
    printList(mergeTwoLists(buildList({1,3,5}), buildList({2,4,6})));

    cout << "\n=== Cycle ===" << endl;
    ListNode* cyc = buildList({1,2,3,4});
    cyc->next->next->next->next = cyc->next;  // create cycle at node 2
    cout << "Has cycle: " << boolalpha << hasCycle(cyc) << endl;
    cout << "Cycle starts at: " << detectCycleStart(cyc)->val << endl;

    cout << "\n=== Remove Nth From End ===" << endl;
    ListNode* removed = removeNthFromEnd(buildList({1,2,3,4,5}), 2);
    printList(removed);  // 1 -> 2 -> 3 -> 5

    cout << "\n=== Palindrome ===" << endl;
    cout << "1->2->2->1 palindrome: " << isPalindrome(buildList({1,2,2,1})) << endl;

    cout << "\n=== Add Two Numbers ===" << endl;
    ListNode* sum = addTwoNumbers(buildList({2,4,3}), buildList({5,6,4}));
    printList(sum);  // 7 -> 0 -> 8

    cout << "\n=== Intersection ===" << endl;
    ListNode* common = buildList({8,9});
    ListNode* a = buildList({1,2});
    ListNode* b = buildList({3,4,5});
    ListNode* tailA = a;
    while (tailA->next) tailA = tailA->next;
    tailA->next = common;
    ListNode* tailB = b;
    while (tailB->next) tailB = tailB->next;
    tailB->next = common;
    cout << "Intersection starts at: " << getIntersectionNode(a, b)->val << endl;

    cout << "\n=== Reorder ===" << endl;
    ListNode* reordered = buildList({1,2,3,4,5});
    reorderList(reordered);
    printList(reordered);  // 1 -> 5 -> 2 -> 4 -> 3

    cout << "\n=== Sort ===" << endl;
    printList(sortList(buildList({4,2,1,3})));  // 1 -> 2 -> 3 -> 4

    return 0;
}
