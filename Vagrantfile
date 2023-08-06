# -*- mode: ruby -*-
# vi: set ft=ruby :

IMAGE_NAME = "ubuntu/focal64"
KUBERNETES_WORKERS = 3
ENV['VAGRANT_SERVER_URL'] = 'https://vagrant.elab.pro'


Vagrant.configure("2") do |config|

    # config.vm.box_download_insecure = true
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
            vb.cpus = 1
        end
        edge.vm.network "private_network", ip: "192.168.53.10"
        edge.vm.network "forwarded_port", guest: 22, host: 2204, id: "ssh"
        edge.ssh.port = 2204
    end

    config.vm.define "aw" do |aw|
        aw.vm.provider "virtualbox" do |vb|
            vb.memory = "1024"
            vb.cpus = 2
        end
        aw.vm.network "private_network", ip: "192.168.51.10"
        aw.vm.network "forwarded_port", guest: 22, host: 2205, id: "ssh"
    end

    config.vm.define "dns" do |dns|
        dns.vm.provider "virtualbox" do |vb|
            vb.memory = "1024"
            vb.cpus = 1
        end
        dns.vm.network "private_network", ip: "192.168.54.10"
        dns.vm.network "forwarded_port", guest: 22, host: 2206, id: "ssh"
    end

    config.vm.define "website" do |website|
        website.vm.provider "virtualbox" do |vb|
            vb.memory = "1024"
            vb.cpus = 2
        end
        website.vm.network "private_network", ip: "192.168.55.10"
        website.vm.network "forwarded_port", guest: 22, host: 2207, id: "ssh"
    end

    config.vm.define "postgresql" do |postgresql|
        postgresql.vm.provider "virtualbox" do |vb|
            vb.memory = "1024"
            vb.cpus = 2
        end
        postgresql.vm.network "private_network", ip: "192.168.56.10"
        postgresql.vm.network "forwarded_port", guest: 22, host: 2208, id: "ssh"
    end
    
    config.vm.define "wedding-site" do |website|
        website.vm.provider "virtualbox" do |vb|
            vb.memory = "1024"
            vb.cpus = 2
        end
        website.vm.network "private_network", ip: "192.168.57.10"
        website.vm.network "forwarded_port", guest: 22, host: 2209, id: "ssh"
    end
end
