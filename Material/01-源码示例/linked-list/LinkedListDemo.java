package com.example.ds;

/**
 * 演示素材：单链表实现（数据结构实验）
 */
public class LinkedListDemo {

    static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;

    public void addFirst(int value) {
        Node node = new Node(value);
        node.next = head;
        head = node;
    }

    public int size() {
        int count = 0;
        Node cur = head;
        while (cur != null) {
            count++;
            cur = cur.next;
        }
        return count;
    }

    public static void main(String[] args) {
        LinkedListDemo list = new LinkedListDemo();
        list.addFirst(3);
        list.addFirst(2);
        list.addFirst(1);
        System.out.println("size=" + list.size());
    }
}
