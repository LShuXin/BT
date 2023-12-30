/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：InterLogin.cpp
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年03月09日
*   描    述：
*
================================================================*/
#include "InterLogin.h"
#include "../DBPool.h"
#include "EncDec.h"

bool CInterLoginStrategy::doLogin(const std::string &strName, const std::string &strPass, IM::BaseDefine::UserInfo& user)
{
    bool bRet = false;
    
    CDBManager* pDBManger = CDBManager::getInstance();
    CDBConn* pDBConn = pDBManger->GetDBConn("teamtalk_slave");
    if (pDBConn)
    {
        string strSql = "select * from IMUser where name='" + strName + "' and status=0";
        CResultSet* pResultSet = pDBConn->ExecuteQuery(strSql.c_str());
        if (pResultSet)
        {
            string strResult, strSalt;
            uint32_t nId, nGender, nDeptId, nStatus;
            string strNick, strAvatar, strEmail, strRealName, strTel, strDomain;
            while (pResultSet->Next())
            {
                nId = pResultSet->GetInt("id");
                strResult = pResultSet->GetString("password");
                strSalt = pResultSet->GetString("salt");
                
                strNick = pResultSet->GetString("nick");
                nGender = pResultSet->GetInt("sex");
                strRealName = pResultSet->GetString("name");
                strDomain = pResultSet->GetString("domain");
                strTel = pResultSet->GetString("phone");
                strEmail = pResultSet->GetString("email");
                strAvatar = pResultSet->GetString("avatar");
                nDeptId = pResultSet->GetInt("departId");
                nStatus = pResultSet->GetInt("status");
            }
            string strInPass = strPass + strSalt;
            char szMd5[33];
            CMd5::MD5_Calculate(strInPass.c_str(), strInPass.length(), szMd5);
            string strOutPass(szMd5);
            // TODO:暂时注释掉密码校验
            if (true || strOutPass == strResult)
            {
                bRet = true;
                user.set_user_id(nId);
                log("nID%d", nId);
                user.set_user_nick_name(strNick);
                log("strNick:%s", strNick.c_str());
                user.set_user_gender(nGender);
                log("gender%d:", nGender);
                user.set_user_real_name(strRealName);
                log("strRealName:%s", strRealName.c_str());
                user.set_user_domain(strDomain);
                log("strDomain:%s", strDomain.c_str());
                user.set_user_tel(strTel);
                log("strTel:%s", strTel.c_str());
                user.set_email(strEmail);
                log("strEmail:%s", strEmail.c_str());
                user.set_avatar_url(strAvatar);
                log("strAvatar:%s", strAvatar.c_str());
                user.set_department_id(nDeptId);
                log("nDeptId:%d", nDeptId);
                user.set_status(nStatus);
                log("nStatus:%d", nStatus);
            }
            delete  pResultSet;
        }
        pDBManger->RelDBConn(pDBConn);
    }
    return bRet;
}