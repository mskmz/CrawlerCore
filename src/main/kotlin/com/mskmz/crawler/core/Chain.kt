package com.mskmz.crawler.core

import java.lang.ref.WeakReference
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//在这里我们实现伪责任链
//实现多链的切换等
class ChainManager {
    init {
        branch("main")
    }

    val branchList by lazy {
        ArrayList<String>()
    }
    val chainMap by lazy {
        HashMap<String, ManagerChain>()
    }
    val eventMap by lazy {
        HashMap<String, WeakReference<EventListen>>()
    }

    val eventMapGlobe by lazy {
        HashMap<String, EventListen>()
    }

    //用于实现切换分支
    fun branch(str: String = "main", chain: ManagerChain = ManagerChain(this)): ManagerChain {
        if (getChain() != null) {
            return getChain()!!
        }
        chainMap[str] = chain
        chain.bindTag(str)
        branchList.add(str)
        return chain
    }

    fun getChain(str: String = getCurrBranch()): ManagerChain? {
        return chainMap[str]
    }

    fun getCurrBranch(): String {
        return branchList.last()
    }

    //终结的chain
    fun finialBranchLast() {
        if (branchList.size > 1) {
            branchList.removeAt(branchList.size - 1)
        }
        if (branchList.size > 1) {
            chainMap[branchList.last()]?.next()
        }
    }

    fun finialBranch(str: String) {
        val i = branchList.indexOf(str)
        var last = branchList.size == i + 1
        if (i != -1) {
            branchList.removeAt(i)
            if (last) {
                chainMap[branchList.last()]?.next()
            }
        }
    }

    fun eventRequest(event: String, reqP: HashMap<String, Any>): HashMap<String, Any>? {
        val listen = if (eventMapGlobe[event] != null) {
            eventMapGlobe[event]
        } else {
            eventMap[event]?.get()
        }
        if (listen == null) {
            return null
        }
        return listen.listen(reqP)
    }

    fun registerEvent(event: String, listen: EventListen, globe: Boolean) {
        if (eventMap[event] != null) {
            eventMap.remove(event)
        }
        if (eventMapGlobe[event] != null) {
            eventMapGlobe.remove(event)
        }
        if (globe) {
            eventMapGlobe[event] = listen
        } else {
            eventMap[event] = WeakReference(listen)
        }
    }
}

//对于Manager方法做出扩展
class ManagerChain(manager: ChainManager) : Chain() {
    val managerWeak: WeakReference<ChainManager> = WeakReference(manager)
    lateinit var tag: String

    init {
        eventRegister("uploadGlobe") {
            if (it != null && it["event"] != null) {
                eventUploadGlobe(it["event"] as String, (it["globe"] ?: false) as Boolean)
            }
            null
        }
    }

    fun bindTag(tag: String) {
        this.tag = tag
    }

    fun eventUploadGlobe(event: String, globe: Boolean) {
        if (eventMap[event] == null) {
            eventMap.remove(event)
            return
        }
        managerWeak.get()?.registerEvent(event, eventMap[event]!!, globe)
    }

    override fun eventRequest(event: String, reqP: HashMap<String, Any>): HashMap<String, Any>? {
        if (reqP["globe"] != null && reqP["globe"] as Boolean) {
            return eventRequestGlobe(event, reqP)
        }
        return eventMap[event]?.listen(reqP)
    }

    fun eventRequestGlobe(event: String, reqP: HashMap<String, Any>): HashMap<String, Any>? {
        return eventMap[event]?.listen(reqP) ?: return managerWeak.get()?.eventRequest(event, reqP)
    }

    override fun next() {
        invoke = true
        if (index < 0) {
            index = 0
        }
        if (index >= chainList.size) {
            managerWeak.get()?.finialBranch(tag)
            index = -1
            return
        }
        chainList[index].invoke()
        index++
    }
}

open class Chain {
    var index = -1
    var invoke = false
    val eventMap by lazy {
        HashMap<String, EventListen>()
    }
    val chainList by lazy {
        ArrayList<Node>()
    }

    fun reset() {
        index = -1
    }

    fun preNode() {
        index--
    }

    fun run() {
        if (invoke) {
            return
        }
        next()
    }

    open fun addNode(node: Node) {
        chainList.add(node)
    }

    open fun next() {
        invoke = true
        if (index < 0) {
            index = 0
        }
        if (index >= chainList.size) {
            index = -1
            return
        }
        chainList[index].invoke()
        index++
    }

    fun eventRegister(event: String, listen: EventListen) {
        eventMap[event] = listen
    }

    fun eventRegister(event: String, listen: (HashMap<String, Any>?) -> HashMap<String, Any>?) {
        eventMap[event] = object : EventListen {
            override fun listen(p: HashMap<String, Any>?): HashMap<String, Any>? {
                return listen(p)
            }
        }
    }

    open fun eventHas(event: String): Boolean {
        return eventMap[event] != null
    }

    open fun eventRequest(event: String, reqP: HashMap<String, Any>): HashMap<String, Any>? {
        return eventMap[event]?.listen(reqP)
    }
}


abstract class Node {
    lateinit var chain: WeakReference<Chain>
    fun bind(chain: Chain) {
        this.chain = WeakReference(chain)
    }

    abstract fun invoke()
    fun next() {
        chain.get()!!.next()
    }

    fun register(event: String, listen: EventListen) {
        chain.get()!!.eventRegister(event, listen)
    }

    fun register(event: String, listen: (HashMap<String, Any>?) -> HashMap<String, Any>?) {
        chain.get()!!.eventRegister(event, listen)
    }

    fun hasEvent(event: String): Boolean {
        return chain.get()!!.eventHas(event)
    }

    fun request(event: String, any: HashMap<String, Any> = HashMap()): HashMap<String, Any>? {
        return chain.get()!!.eventRequest(event, any)
    }
}

interface EventListen {
    fun listen(p: HashMap<String, Any>? = null): HashMap<String, Any>?
}