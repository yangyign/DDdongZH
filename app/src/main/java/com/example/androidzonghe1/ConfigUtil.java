package com.example.androidzonghe1;

import android.app.Application;

import com.example.androidzonghe1.activity.Track.TrackApplication;
import com.example.androidzonghe1.entity.lpyWork.DayTrip;
import com.example.androidzonghe1.entity.lpyWork.Driver;
import com.example.androidzonghe1.entity.lpyWork.Messages;
import com.example.androidzonghe1.entity.lpyWork.Order;
import com.example.androidzonghe1.entity.lpyWork.SameSchoolRoute;
import com.example.androidzonghe1.entity.xtWork.RvFragmentMy;
import com.example.androidzonghe1.entity.yjWork.Parent;
import com.example.androidzonghe1.entity.yyWork.DriverOrder;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {
    //当前密码
    public static String pwd="";
    //判断当前用户是否登录
    public static boolean isLogin = false;
    //当前用户名
    public static String userName = "";
    //当前手机号
    public static String phone="";
    //当前用户与孩子的关系
    public static String relationship = "";

    //当前搜索学校
    public static String school="";
    //搜索学校的经纬度
    public static double latitude = 0;
    public static double longitude = 0;
    public static Parent parent = new Parent();

    //选定司机的手机号
    public static String driverPhone = "";

    //徐婷连接到服务器
//    public static final String xt = "http://10.7.90.141:8080/Dingdongg/";
    public static final String xt = "http://10.7.90.227:8080/Dingdongg/";
    //任佳旭连接服务器
//    public static final String Url = "http://192.168.43.52:8080/DingDong/";
//    public static final String Url = "http://10.7.90.67:8080/DingDong/";
//
//    public static final String url = "http://10.7.90.67:8080/DingDong/";
//
//    public static final String URL = "http://59.110.228.91:8080/DingDong/";

    public static boolean adapterFlag =  true;


    ///是否下单
    public static boolean flagOrder = false;

    //下单操作进行司机选择（跳转界面）
    public static boolean flagChooseDriver = false;

    public static List<Order> orders = new ArrayList<>();
    public static void initOrders(){
        for(int i = 0 ; i < 20; i++){
            Order order = new Order();
            order.setName("刘培焱"+i);
            order.setRoute("成都->北京->上海"+i);
            ConfigUtil.orders.add(order);
        }
    }
    public static List<Driver> drivers = new ArrayList<>();
    public static void initDrivers(){
        for(int i = 0 ; i < 10; i++){
            Driver driver = new Driver();
//            driver.setImg(R.drawable.driver_img);
            driver.setName("刘培焱");
            driver.setAge(18);
            if (i%3 != 0){
                driver.setStatus("空闲");
            } else {
                driver.setStatus("忙碌");
            }
            driver.setCar("兰博基尼");
            driver.setStyle("绿色");
            driver.setExperience("十年老司机，从不翻车");
            driver.setPhone("18830909589");
            ConfigUtil.drivers.add(driver);
        }
    }
    public static List<SameSchoolRoute> routes = new ArrayList<>();
    public static void initRoutes(){
        for(int i = 0 ; i < 10; i++){
            SameSchoolRoute route = new SameSchoolRoute();
            route.setSchool("上海实验小学"+i);
            route.setPeopleNow(i);
            route.setPeopleTotal(i+1);
            route.setRouteState("可加入");
            route.setCommunityFirst("兴泉社区"+i);
            route.setDistanceCommunityFirst(3);
            route.setCommunitySecond("金华园"+i);
            route.setDistanceCommunitySecond(4);
            ConfigUtil.routes.add(route);
        }
    }

    public static List<DayTrip> trips = new ArrayList<>();
    public static void initTrips(){
        for(int i = 0; i < 10 ; i++){
            DayTrip dayTrip = new DayTrip();
            dayTrip.setGoOrCome("放学"+i);
            dayTrip.setDate("2020-12-11");
            dayTrip.setTimeBegin("16:40");
            dayTrip.setTripState("运行中");
            dayTrip.setTimeEnd("17:00");
            dayTrip.setPlaceBegin("徐汇区实验小学");
            dayTrip.setPlaceEnd("望春园西门");
            ConfigUtil.trips.add(dayTrip);
        }
    }
    public static List<DriverOrder> trip = new ArrayList<>();
    public static void initTrip(){
        for(int i = 0; i < 10 ; i++){
            DriverOrder dayTrip = new DriverOrder();
            dayTrip.setAddress("放学"+i);
            dayTrip.setDate("2020-12-11");
            dayTrip.setTime("16:40");
            dayTrip.setState("运行中");
            dayTrip.setEndTime("17:00");
            dayTrip.setFrom("徐汇区实验小学");
            dayTrip.setTo("望春园西门");
            ConfigUtil.trip.add(dayTrip);
        }
    }

    public static List<Messages> messages = new ArrayList<>();
    public static void initMessages(){
        for(int i = 0; i < 10 ; i++){
            Messages message = new Messages();
            message.setTitle("这春节是放多少天假啊？？？31？？？？？？？61？？？？");
            message.setDate("2020-11-16");
            message.setType("本地资讯");
            ConfigUtil.messages.add(message);
        }
    }

    public static List<RvFragmentMy> mys = new ArrayList<>();


}
