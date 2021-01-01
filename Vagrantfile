# -*- mode: ruby -*-
# vi: set ft=ruby :

IMAGE_NAME = "ubuntu/xenial64"
KUBERNETES_WORKERS = 3

Vagrant.configure("2") do |config|

    config.vm.box = IMAGE_NAME
    config.vm.provider "virtualbox" do |v|
        v.memory = 2048
        v.cpus = 4
    end

    config.vm.define "k8s-master" do |master|
        master.vm.network "private_network", ip: "192.168.50.10"
        master.vm.hostname = "k8s-master"
        master.vm.network "forwarded_port", guest: 22, host: 2200, id: "ssh"
        master.ssh.port = 2200
    end

    (1..KUBERNETES_WORKERS).each do |i|
        config.vm.define "node-#{i}" do |node|
            node.vm.network "private_network", ip: "192.168.50.#{i + 10}"
            node.vm.network "forwarded_port", guest: 22, host: 2200 + i, id: "ssh"
            node.ssh.port = 2200 + i
            node.vm.hostname = "node-#{i}"
            node.vm.provider "virtualbox" do |v|
              v.memory = 4096
              v.cpus = 4
            end
        end
    end

    config.vm.define "edge" do |edge|
        edge.vm.provider "virtualbox" do |vb|
            vb.memory = "1024"
        end
        edge.vm.network "private_network", ip: "192.168.53.10"
        edge.vm.network "forwarded_port", guest: 22, host: 2204, id: "ssh"
        edge.ssh.port = 2204
    end
end