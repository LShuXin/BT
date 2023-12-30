/*================================================================
*     Copyright (c) 2015年 lanhu. All rights reserved.
*   
*   文件名称：DepartModel.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年03月12日
*   描    述：
*
#pragma once
================================================================*/
#ifndef __DEPARTMODEL_H__
#define __DEPARTMODEL_H__

#include "ImPduBase.h"
#include "IM.BaseDefine.pb.h"

class CDepartModel
{
public:
    static CDepartModel* getInstance();
    ~CDepartModel() {}

    // 获取指定时间之后发生变动的部门 id 列表
    void getChgedDeptId(uint32_t& nLastTime, list<uint32_t>& lsChangedIds);

    // 获取所有部门的部门 id 列表以及部门信息列表
    void getDepts(list<uint32_t>& lsDeptIds, list<IM::BaseDefine::DepartInfo>& lsDepts);

    // 根据 id 获取指定部门的信息
    void getDept(uint32_t nDeptId, IM::BaseDefine::DepartInfo& cDept);
private:
    CDepartModel() {};
private:
    static CDepartModel* m_pInstance;
};

#endif /*defined(__DEPARTMODEL_H__) */
