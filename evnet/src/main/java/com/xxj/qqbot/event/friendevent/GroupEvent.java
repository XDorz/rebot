package com.xxj.qqbot.event.friendevent;

import cn.hutool.core.img.gif.AnimatedGifEncoder;
import cn.hutool.core.img.gif.GifDecoder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.config.BotInfo;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import com.xxj.qqbot.event.aop.init.BotEvents;
import com.xxj.qqbot.event.aop.init.BotExceptionHandler;
import com.xxj.qqbot.history.entity.YunshiDO;
import com.xxj.qqbot.history.repo.YunshiDORepo;
import com.xxj.qqbot.util.botconfig.config.Apis;
import com.xxj.qqbot.util.botconfig.config.Yunshi;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEventAppend;
import com.xxj.qqbot.util.common.BeastTransUtil;
import com.xxj.qqbot.util.common.BotMessageInfo;
import com.xxj.qqbot.util.common.CommonEventToolUtil;
import com.xxj.qqbot.util.common.ImageUploadUtil;
import com.xxj.qqbot.util.common.ImageUtil;
import com.xxj.qqbot.util.common.ValUtil;
import io.korhner.asciimg.image.AsciiImgCache;
import io.korhner.asciimg.image.character_fit_strategy.ColorSquareErrorFitStrategy;
import io.korhner.asciimg.image.character_fit_strategy.StructuralSimilarityFitStrategy;
import io.korhner.asciimg.image.converter.AsciiToImageConverter;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.ImageType;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@BotEvents
public class GroupEvent implements BotExceptionHandler {

    @Autowired
    private YunshiDORepo yunshiDORepo;

    //抽象功能使用字体
    private static final String FONTNAME="黑体";

    //Courier
    private static AsciiImgCache defaultFontCache = AsciiImgCache.create(new Font(FONTNAME,Font.PLAIN, 7));

    private static AsciiToImageConverter defaultConverter=new AsciiToImageConverter(defaultFontCache, new StructuralSimilarityFitStrategy());

    @ListenEvent(startWith = "运势",quoteReply = true,waitMessage = "少女折寿中...",needWait = true)
    public void yunshi(GroupMessageEvent event, MessageChainBuilder builder){
        YunshiDO yunshiDO = yunshiDORepo.findAllByQqIdAndExpireTimeAfter(event.getSender().getId(), new Date());
        if (yunshiDO!=null){
            builder.append(Image.fromId(yunshiDO.getImageId()));
            return;
        }
        Yunshi.Fortune fortune = ValUtil.getRandomVal(Yunshi.fortunes);
        File file = ValUtil.getRandomVal(Yunshi.genshinBasic);
        String key = file.getName().replace(".png", "").replace("2","").replace("3","");
        String peopleName=Yunshi.genshinNameReflect.get(key);
        String boardName = Yunshi.genshinReflect.get(peopleName);
        BufferedImage board = Yunshi.genshinBoard.get(boardName);
        //绘制运势
        board= ImageUtil.drawString(board,Yunshi.genshinFont.get("life"),100f,fortune.getLuck()
                ,40,45,375,205,0,20, Color.WHITE,ImageUtil.STRING_CENTER);
        //绘制提谏言
        board = ImageUtil.drawString(board, Yunshi.genshinFont.get("shouxie"), 50f, fortune.getContent()
                , 30, 400, 390, 450, 10, 40, Color.BLACK, ImageUtil.STRING_VERTICAL, ImageUtil.STRING_REVERSE,ImageUtil.STRING_CENTER,ImageUtil.STRING_UP);
        //绘制人物名字
        board=ImageUtil.drawString(board,Yunshi.genshinFont.get("sakura"),30f,peopleName,30,600,40,240
                ,3,0,new Color(94,201,182),ImageUtil.STRING_LEFT,ImageUtil.STRING_DOWN,ImageUtil.STRING_VERTICAL);
        //与底板结合
        board = ImageUtil.draw(file, board, 90, 90);
        //绘制logo
        board = ImageUtil.draw(board, Yunshi.genshinTitle, (board.getWidth() - Yunshi.genshinTitle.getWidth()) / 2, 50);
        Image image = ImageUploadUtil.upload(board);
        builder.append(image);
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,55);
        YunshiDO.YunshiDOBuilder yunshiBuilder = YunshiDO.builder()
                .id(IdUtil.getSnowflakeNextId())
                .qqId(event.getSender().getId())
                .expireTime(calendar.getTime())
                .lucky(fortune.getLuck())
                .context(fortune.getContent())
                .nameCard(event.getSenderName())
                .groupId(event.getSubject().getId())
                .groupName(event.getSubject().getName())
                .imageId(image.getImageId());
        yunshiDORepo.save(yunshiBuilder.build());
    }

    @ListenEvent(startWith = "cos",quoteReply = false,atSender = true,forwardMessage = true)
    public void cos(GroupMessageEvent event, MessageChainBuilder builder, BotMessageInfo info){
        String num = info.getParams().getOrDefault("param1", "1");
        int sendNum = Math.min(Integer.parseInt(num),15);
        for (int i = 0; i < sendNum; i++) {
            String url = HttpUtil.get(Apis.cosApi);
            HttpResponse response = HttpUtil.createGet(url).execute();
            InputStream stream = response.bodyStream();
            Image image = ImageUploadUtil.upload(stream);
            builder.append(image);
        }
    }

    @ListenEvent(startWith = "5k",quoteReply = false)
    public void FiveK(MessageChainBuilder builder,GroupMessageEvent event,BotMessageInfo info){
        String top = info.getParams().get("param1");
        String buttom=info.getParams().get("param2");
        event.getSubject().sendMessage("开始请求api");
        HttpResponse response = HttpUtil.createGet(MessageFormat.format(Apis.fivekApi, top, buttom)).execute();
        builder.append(ImageUploadUtil.upload(response.bodyStream()));
    }

    @ListenEvent(startWith = "兽语",appendToken = "兽语",quoteReply = false,alias = "兽音译者")
    public Integer beast(MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info){
        String context = ValUtil.getMessageContext(event.getMessage());
        String[] split = context.split(" ");
        if(split.length>1){
            switch (split[1]){
                case "encode":
                    builder.append(new PlainText("请发送您想要变成兽语的原文"));
                    return 0;
                case "decode":
                case "翻译":
                    builder.append(new PlainText("请发送您想要翻译回原文的兽语"));
                    return 1;
                default:
                    builder.append(new PlainText("命令后请跟上 encode/decode/翻译 中的一种"));
                return -1;
            }
        }
        builder.append("命令后请跟上 encode/decode/翻译 中的一种");
        return -1;
    }

    @ListenEventAppend(token = "兽语",quoteReply = false,maxListenTimeMinute = 2,atSender = true)
    public Object transBeast (MessageChainBuilder builder, GroupMessageEvent event,Object obj){
        Integer result = (Integer) obj;
        if(result==0){
            builder.append(new PlainText(BeastTransUtil.encode(event.getMessage().contentToString())));
        }else if(result==1){
            builder.append(new PlainText(BeastTransUtil.decode(event.getMessage().contentToString())));
        }
        return false;
    }

    @ListenEvent(startWith = "抽象",appendToken = "抽象")
    public Object abs (MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info) throws IOException {
        String size = info.getParams().getWithoutThrow("param1");
        AsciiToImageConverter asciiConverter=defaultConverter;
        if(size!=null){
            Integer fontSize = Integer.valueOf(size);
            fontSize=Math.max(1,fontSize);
            fontSize=Math.min(fontSize,16);
            AsciiImgCache fontCache = AsciiImgCache.create(new Font(FONTNAME,Font.PLAIN, fontSize));
            asciiConverter=new AsciiToImageConverter(fontCache,new StructuralSimilarityFitStrategy());
            builder.append(new PlainText("您想要抽象的粒度为："+fontSize+"\n"));
        }
        if(info.hasAt()){
            for (At at : info.getAts()) {
                String photoUrl = event.getSubject().get(at.getTarget()).getAvatarUrl();
                InputStream inputStream = HttpUtil.createGet(photoUrl).execute().bodyStream();
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                BufferedImage image = asciiConverter.convertImage(bufferedImage);
                builder.append(ImageUploadUtil.upload(image));
            }
            return 0;
        }
        builder.append(new PlainText("请发送你想要抽象的图像~"));
        return asciiConverter;
    }

    @ListenEventAppend(token = "抽象",quoteReply = false,maxListenTimeMinute = 2)
    public Object absAppend (MessageChainBuilder builder, GroupMessageEvent event,Object obj,BotMessageInfo info) throws IOException{
        if(obj instanceof Integer) return false;
        if(!info.hasImage()) return false;
        if(obj instanceof AsciiToImageConverter){
            AsciiToImageConverter asciiConverter = (AsciiToImageConverter) obj;
            for (Image image : info.getImages()) {
                String url = Image.queryUrl(image);
                InputStream inputStream = HttpUtil.createGet(url).execute().bodyStream();
                if(image.getImageType().equals(ImageType.GIF)){
                    GifDecoder decoder=new GifDecoder();
                    decoder.read(inputStream);
                    AnimatedGifEncoder encoder=new AnimatedGifEncoder();
                    ByteArrayOutputStream bout=new ByteArrayOutputStream();
                    encoder.start(bout);
                    encoder.setRepeat(0);
                    encoder.setDelay(decoder.getDelay(1));
                    for (int i = 0; i < decoder.getFrameCount(); i++) {
                        BufferedImage frame = decoder.getFrame(i);
                        encoder.addFrame(asciiConverter.convertImage(frame));
                    }
                    encoder.finish();
                    ByteArrayInputStream bin=new ByteArrayInputStream(bout.toByteArray());
                    builder.append(ImageUploadUtil.upload(bin));
                }else {
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    BufferedImage img = asciiConverter.convertImage(bufferedImage);
                    builder.append(ImageUploadUtil.upload(img));
                }
            }
        }
        return false;
    }

    @ListenEvent(startWith = "涩图",blackListType = false,needWait = true,autoSend = false,waitMessage = "少女折寿中...")
    public void sexPic (GroupMessageEvent event,BotMessageInfo info){
        List<MessageChain> sexPicMessages = CommonEventToolUtil.getSexPicWithoutInfo(info, false,BotInfo.maxPic);
        if(sexPicMessages==null){
            event.getSubject().sendMessage("搜索无结果，换个关键词/标签 试试吧~");
            return;
        }
        ForwardMessageBuilder builder=new ForwardMessageBuilder(event.getSender());
        builder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
        for (MessageChain sexPicMessage : sexPicMessages) {
            builder.add(event.getSender(),sexPicMessage);
        }
        event.getSubject().sendMessage(builder.build());
    }

    @ListenEvent(startWith = "色图",autoSend = false,blackListType = false)
    public void saxPic (MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info) throws IOException {
        Friend friend = BotFrameworkConfig.bot.getFriend(event.getSender().getId());
        if (friend==null){
            builder.append(new QuoteReply(event.getSource()));
            builder.append("对不起，你现在还不是"+BotInfo.bname+"的好友，快加上好友一起看色图吧，别忘了好友验证填『"+BotInfo.verifyAddFriend+"』哦~");
            event.getSubject().sendMessage(builder.build());
            return;
        }
        List<MessageChain> sexPicMessages = CommonEventToolUtil.getSexPic(info, true,BotInfo.maxPic);
        if(sexPicMessages==null){
            event.getSubject().sendMessage("搜索无结果，换个关键词/标签 试试吧~");
            return;
        }
        ForwardMessageBuilder forwardBuilder=new ForwardMessageBuilder(event.getSender());
        forwardBuilder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
        for (MessageChain sexPicMessage : sexPicMessages) {
            forwardBuilder.add(event.getSender(),sexPicMessage);
        }
        MessageReceipt<Member> receipt = event.getSender().sendMessage(forwardBuilder.build());
        receipt.recallIn(BotInfo.r18RecallTime*1000);
        event.getSubject().sendMessage("太色啦>_<，已经私发你了，注意查看私聊哦~~~");
    }

    @Override
    public void handleException(AbstractEvent event, Throwable throwable) {
        if(event instanceof GroupMessageEvent){
            ((GroupMessageEvent) event).getSubject().sendMessage("ERROR:"+throwable.getMessage());
        }
    }
}
