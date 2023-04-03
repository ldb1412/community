package com.nowcoder.community.util;/**
 * @author DB1412
 * @create 2023-03-22 19:19
 */

import com.mysql.cj.jdbc.SuspendableXAConnection;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

/**
 *@ClassName SensitiveFilter
 *@Description 敏感词过滤
 *@Author DB1412
 *@Date 2023-03-22 19:19
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    @PostConstruct//初始化方法，当容器实例化这个bean以后，在调用该类的构造器以后就会自动调用，在服务启动的时候就会调用
    public void init(){
        try (//获取敏感词文件
             InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null){//读取一行数据（敏感词是一行行写的）
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){//循环读取字符
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null){//判断当前节点的子节点是否存在该字符
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode = subNode;

            if(i == keyword.length() - 1){//如果读取到最后一个字符，则要标记为结束
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * @description: 过滤敏感词
     * @param: text 待过滤的文本
     * @return: java.lang.String 过滤后的文本
     * @author DB1412
     * @date: 20:07 2023-03-22
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1(遍历前缀树)
        TrieNode tempNode = rootNode;
        //指针2（遍历字符串）
        int begin = 0;
        //指针3（当发现敏感词的第一个字符后，由该指针逐渐向后判断由该字符开头的字符串是否为敏感词）
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();
        while(begin < text.length()){
            if(position < text.length()) {//因为position可能先走到了字符串的结尾，而begin指针还没有走到
                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {//判断是符号
                    //如果当前节点是根节点，则打印该字符
                    if (tempNode == rootNode) {
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;//当发现敏感词的第一个字符以后，为了防止用户通过加入特殊符号的手段逃过检查，这里需要跳过符号
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置(begin先 +1 然后赋值给position)
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = rootNode;
                }
                // 发现敏感词的字符，则检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{//当position走到底还未发现敏感词时，需要回到begin指针所在的位置，begin指针也需要加一
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;//同时将前缀树指针指向根节点
            }
        }
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

}
