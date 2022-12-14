package com.xxj.qqbot.event.event;

import cn.hutool.core.img.gif.AnimatedGifEncoder;
import cn.hutool.core.img.gif.GifDecoder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.xxj.qqbot.event.aop.init.BotEvents;
import com.xxj.qqbot.event.aop.init.BotExceptionHandler;
import com.xxj.qqbot.history.entity.YunshiDO;
import com.xxj.qqbot.history.repo.YunshiDORepo;
import com.xxj.qqbot.util.botconfig.config.Apis;
import com.xxj.qqbot.util.botconfig.config.BotFrameworkConfig;
import com.xxj.qqbot.util.botconfig.config.BotInfo;
import com.xxj.qqbot.util.botconfig.config.Yunshi;
import com.xxj.qqbot.util.botconfig.config.constant.EventFunctionType;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEvent;
import com.xxj.qqbot.util.botconfig.functioncompent.ListenEventAppend;
import com.xxj.qqbot.util.common.BeastTransUtil;
import com.xxj.qqbot.util.common.BotMessageInfo;
import com.xxj.qqbot.util.common.CommonEventToolUtil;
import com.xxj.qqbot.util.common.ImageUploadUtil;
import com.xxj.qqbot.util.common.ImageUtil;
import com.xxj.qqbot.util.common.ValUtil;
import com.xxj.qqbot.util.music.MusicInfo;
import com.xxj.qqbot.util.music.MusicSource;
import com.xxj.qqbot.util.music.QQSource;
import io.korhner.asciimg.image.AsciiImgCache;
import io.korhner.asciimg.image.character_fit_strategy.StructuralSimilarityFitStrategy;
import io.korhner.asciimg.image.converter.AsciiToImageConverter;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
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
public class GroupEvent implements BotExceptionHandler{

    @Autowired
    private YunshiDORepo yunshiDORepo;

    //????????????????????????
    private static final String FONTNAME="??????";

    //Courier
    private static AsciiImgCache defaultFontCache = AsciiImgCache.create(new Font(FONTNAME,Font.PLAIN, 7));

    private static AsciiToImageConverter defaultConverter=new AsciiToImageConverter(defaultFontCache, new StructuralSimilarityFitStrategy());

    @ListenEvent(startWith = "??????",quoteReply = true,waitMessage = "???????????????...",needWait = true,help = "${commonHelp%??????}",alias = "??????")
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
        //????????????
        board= ImageUtil.drawString(board,Yunshi.genshinFont.get("life"),100f,fortune.getLuck()
                ,40,45,375,205,0,20, Color.WHITE,ImageUtil.STRING_CENTER);
        //???????????????
        board = ImageUtil.drawString(board, Yunshi.genshinFont.get("shouxie"), 50f, fortune.getContent()
                , 30, 400, 390, 450, 10, 40, Color.BLACK, ImageUtil.STRING_VERTICAL, ImageUtil.STRING_REVERSE,ImageUtil.STRING_CENTER,ImageUtil.STRING_UP);
        //??????????????????
        board=ImageUtil.drawString(board,Yunshi.genshinFont.get("sakura"),30f,peopleName,30,600,40,240
                ,3,0,new Color(94,201,182),ImageUtil.STRING_LEFT,ImageUtil.STRING_DOWN,ImageUtil.STRING_VERTICAL);
        //???????????????
        board = ImageUtil.draw(file, board, 90, 90);
        //??????logo
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

    @ListenEvent(startWith = "cos",quoteReply = false,forwardMessage = true,alias = "cos",help = "${?????????cos [??????????????????]\n??????????????????????????????}",type = EventFunctionType.sex)
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

    @ListenEvent(startWith = "5k",quoteReply = false,alias = "5k",help = "${commonHelp%fiveK}")
    public void FiveK(MessageChainBuilder builder,GroupMessageEvent event,BotMessageInfo info){
        String top = info.getParams().get("param1");
        String buttom=info.getParams().get("param2");
        HttpResponse response = HttpUtil.createGet(MessageFormat.format(Apis.fivekApi, top, buttom)).execute();
        builder.append(ImageUploadUtil.upload(response.bodyStream()));
    }

    @ListenEvent(startWith = "??????",appendToken = "??????",quoteReply = false,alias = "????????????",help = "${toolHelp%??????}",type = EventFunctionType.tool)
    public Integer beast(MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info){
        String context = ValUtil.getMessageContext(event.getMessage());
        String[] split = context.split(" ");
        if(split.length>1){
            switch (split[1]){
                case "encode":
                    builder.append(new PlainText("???????????????????????????????????????"));
                    return 0;
                case "decode":
                case "??????":
                    builder.append(new PlainText("??????????????????????????????????????????"));
                    return 1;
                default:
                    builder.append(new PlainText("?????????????????? encode/decode/?????? ????????????"));
                return -1;
            }
        }
        builder.append("?????????????????? encode/decode/?????? ????????????");
        return -1;
    }

    @ListenEventAppend(token = "??????",quoteReply = false,maxListenTimeMinute = 2,atSender = true)
    public Object transBeast (MessageChainBuilder builder, GroupMessageEvent event,Object obj){
        Integer result = (Integer) obj;
        if(result==0){
            builder.append(new PlainText(BeastTransUtil.encode(event.getMessage().contentToString())));
        }else if(result==1){
            builder.append(new PlainText(BeastTransUtil.decode(event.getMessage().contentToString())));
        }
        return false;
    }

    @ListenEvent(startWith = "??????",blackListType = false,needWait = true,autoSend = false,waitMessage = "???????????????...",type = EventFunctionType.sex,help = "${sexHelp%??????}${helpSpecial}")
    public void sexPic (GroupMessageEvent event,BotMessageInfo info){
        List<MessageChain> sexPicMessages = CommonEventToolUtil.getSexPicWithoutInfo(info, false,BotInfo.maxPic);
        if(sexPicMessages==null){
            event.getSubject().sendMessage("?????????????????????????????????/?????? ?????????~");
            return;
        }
        ForwardMessageBuilder builder=new ForwardMessageBuilder(event.getSender());
        builder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
        for (MessageChain sexPicMessage : sexPicMessages) {
            builder.add(event.getSender(),sexPicMessage);
        }
        MessageReceipt<Group> receipt = event.getSubject().sendMessage(builder.build());
        receipt.recallIn(BotInfo.r18RecallTime*1000);
    }

    @ListenEvent(startWith = "??????",autoSend = false,blackListType = false,type = EventFunctionType.sex,help = "${sexHelp%??????}${helpSpecial}")
    public void saxPic (MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info) throws IOException {
        Friend friend = BotFrameworkConfig.bot.getFriend(event.getSender().getId());
        if (friend==null){
            builder.append(new QuoteReply(event.getSource()));
            builder.append("??????????????????????????????"+ BotInfo.bname+"???????????????????????????????????????????????????????????????????????????"+BotInfo.verifyAddFriend+"??????~");
            event.getSubject().sendMessage(builder.build());
            return;
        }
        List<MessageChain> sexPicMessages = CommonEventToolUtil.getSexPic(info, true,BotInfo.maxPic);
        if(sexPicMessages==null){
            event.getSubject().sendMessage("?????????????????????????????????/?????? ?????????~");
            return;
        }
        ForwardMessageBuilder forwardBuilder=new ForwardMessageBuilder(event.getSender());
        forwardBuilder.setDisplayStrategy(BotFrameworkConfig.forwardDisplay);
        for (MessageChain sexPicMessage : sexPicMessages) {
            forwardBuilder.add(event.getSender(),sexPicMessage);
        }
        MessageReceipt<Member> receipt = event.getSender().sendMessage(forwardBuilder.build());
        receipt.recallIn(BotInfo.r18RecallTime*1000);
        event.getSubject().sendMessage("?????????>_<?????????????????????????????????????????????~~~");
    }

    @ListenEvent(startWith = "??????",appendToken = "??????",help = "${commonHelp%??????}")
    public Object asciiArt (MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info) throws IOException {
        String size = info.getParams().getWithoutThrow("param1");
        AsciiToImageConverter asciiConverter=defaultConverter;
        if(size!=null){
            Integer fontSize = Integer.valueOf(size);
            fontSize=Math.max(1,fontSize);
            fontSize=Math.min(fontSize,16);
            AsciiImgCache fontCache = AsciiImgCache.create(new Font(FONTNAME,Font.PLAIN, fontSize));
            asciiConverter=new AsciiToImageConverter(fontCache,new StructuralSimilarityFitStrategy());
            builder.append(new PlainText("??????????????????????????????"+fontSize+"\n"));
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
        builder.append(new PlainText("?????????????????????????????????~"));
        return asciiConverter;
    }

    @ListenEventAppend(token = "??????",quoteReply = false,maxListenTimeMinute = 2)
    public Object asciiArtAppend (MessageChainBuilder builder, GroupMessageEvent event,Object obj,BotMessageInfo info) throws IOException{
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

    @ListenEvent(startWith = "??????",help = "${toolHelp%??????}",type = EventFunctionType.tool,appendToken = "??????")
    public Object orderSong (MessageChainBuilder builder, GroupMessageEvent event,BotMessageInfo info) throws IOException {
        MusicSource source = new QQSource();
//        List<String> list = source.getMusicList("????????????");
//        int i=0;
//        for (String s : list) {
//            i++;
//            builder.append(new PlainText(i+"."+s+"\n"));
//        }
        MusicInfo info1 = source.getMusicInfo("??????????????????",0,true);
        builder.append(new PlainText(info1.toString()+"\n\n"));
        return false;
    }

    @ListenEventAppend(token = "??????",appendToken = "??????",autoSend = false)
    public Object orderSong (MessageChainBuilder builder, GroupMessageEvent event,Object obj) throws IOException {
        return false;
    }

    @Override
    public void handleException(AbstractEvent event, Throwable throwable) {
        if(event instanceof GroupMessageEvent){
            ((GroupMessageEvent) event).getSubject().sendMessage("ERROR:"+throwable.getMessage());
        }
    }
}
