$TTL 604800
@                   IN      SOA         ns.alpha.belenot.com.       root.alpha.belenot.com. 2 604800 86400 2419200 604800
@                   IN      NS          ns.alpha.belenot.com.
ns                  IN      A           192.168.54.10
edge                IN      A           192.168.53.10
aw                  IN      A           192.168.51.10
nexus               IN      A           192.168.52.10

node-0.k8s          IN      A           192.168.50.10
node-1.k8s          IN      A           192.168.50.11
node-2.k8s          IN      A           192.168.50.12
node-3.k8s          IN      A           192.168.50.13

