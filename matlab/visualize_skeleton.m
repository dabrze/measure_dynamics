function [] = visualize_skeleton ()

X = load('X01_one.txt');

sm = sum(X(1,:));
m = size(X,1);
W = [ [ 1,  1,  1]', [-1, -1,  1]', [-1,  1, -1]', [ 1, -1, -1]' ];
WX = (W*(X/sm)')';
rgb_colours = zeros(m,3);


val = 0;
idx = find( ((X(:,1)==val)&(X(:,2)==val)) | ((X(:,1)==val)&(X(:,3)==val)) | ((X(:,1)==val)&(X(:,4)==val)) | ((X(:,2)==val)&(X(:,3)==val)) | ((X(:,2)==val)&(X(:,4)==val)) | ((X(:,3)==val)&(X(:,4)==val)) );
size(idx)

WY = [ 
 1,  1,  1; 
-1, -1,  1; 
-1,  1, -1; 
 1, -1, -1; 
-1, -1,  1; 
 1, -1, -1; 
 1,  1,  1; 
-1,  1, -1;
];

fs = 24;
scatter3(WX(idx,1),WX(idx,2),WX(idx,3),1,rgb_colours(idx,:),'filled'); 
hold on; 
plot3(WY(:,1),WY(:,2),WY(:,3),'k-','LineWidth',2); 
p = 0;
PLz = [ 
 1,  1,  p;
-1,  1,  p;
-1, -1,  p;
 1, -1,  p;
 1,  1,  p;
];
PLw = [ 
 p,  1,  p;
 1,  p,  p;
-p, -1,  p;
-1, -p,  p;
 p,  1,  p;
];
plot3(PLz(:,1),PLz(:,2),PLz(:,3),'k:','LineWidth',1.5); 
plot3(PLw(:,1),PLw(:,2),PLw(:,3),'k-','LineWidth',1.5); 
p = -2/3;
PLz = [ 
 1,  1,  p;
-1,  1,  p;
-1, -1,  p;
 1, -1,  p;
 1,  1,  p;
];
PLw = [ 
 p,  1,  p;
 1,  p,  p;
-p, -1,  p;
-1, -p,  p;
 p,  1,  p;
];
plot3(PLz(:,1),PLz(:,2),PLz(:,3),'k:','LineWidth',1.5); 
plot3(PLw(:,1),PLw(:,2),PLw(:,3),'k-','LineWidth',1.5);

K1 = [ 
0, 1, 0;
-1, -1, +1;
];
K2 = [ 
1, 1, 1;
-1, 0, 0;
];
K3 = [ 
-1, 1, -1;
0, 0, 1;
];
K4 = [ 
-1/3, 1/3, 1/3;
1, -1,-1;
];

PTS = [
1 1 1 1;
1 1 1 0;
0 0 1 1;
0 0 0 1;
]

PTS = [
1 0 0 0;
1 0 1 0;
1 1 1 0;
1 1 1 1;
]

WPTS = (W*(diag(1./sum(PTS')')*PTS)')';

fs = 24;
scatter3(WX(idx,1),WX(idx,2),WX(idx,3),1,rgb_colours(idx,:),'filled'); 
hold on; 
plot3(WY(:,1),WY(:,2),WY(:,3),'k-','LineWidth',2); 
plot3(K1(:,1),K1(:,2),K1(:,3),'k:','LineWidth',1.5); 
plot3(K2(:,1),K2(:,2),K2(:,3),'k:','LineWidth',1.5); 
plot3(K3(:,1),K3(:,2),K3(:,3),'k:','LineWidth',1.5); 
plot3(K4(:,1),K4(:,2),K4(:,3),'k--','LineWidth',1); 
scatter3(WPTS(:,1),WPTS(:,2),WPTS(:,3),64,'k','filled'); 


hold off;
acbd_gradient_view(gcf,12,1);

