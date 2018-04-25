function [] = show_tetrahedron ( fontsize, linesize )

if ~(exist('fontsize','var')),
	fontsize = 16;
end;

if ~(exist('linesize','var')),
	linesize = 1;
end;

%set(gcf,'Position',[200 80 400 360]); % asus
ticklabels = {'-1';'';'0';'';'+1'};

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

X = load('X01_one.txt');

sm = sum(X(1,:));
m = size(X,1);

% Y:	d	r	l	f	s	b	A	Z	c1	chi2	c11	c2	c3	c4	M	N	R	G
% Z:	s1	s2	chi2	p	(X-Eij)_{1,1}	v	d

W = [ [ 1,  1,  1]', [-1, -1,  1]', [-1,  1, -1]', [ 1, -1, -1]' ];
WX = (W*(X/sm)')';

colours = zeros(m,1);
%for q=1:m,
%	colours(q) = norm(WX(q,:));
%end;

rgb_colours = zeros(m,3);
%for q=1:m,
%	rgb_colours(q,:) = cm2rgb(colours(q),1,sqrt(3)+0.01,jet(64));
%end;

val = 0;
idx = find( ((X(:,1)==val)&(X(:,2)==val)) | ((X(:,1)==val)&(X(:,3)==val)) | ((X(:,1)==val)&(X(:,4)==val)) | ((X(:,2)==val)&(X(:,3)==val)) | ((X(:,2)==val)&(X(:,4)==val)) | ((X(:,3)==val)&(X(:,4)==val)) );

WY = [ 
 1,  1,  1; %A
-1, -1,  1; %C
-1,  1, -1; %B
 1, -1, -1; %D
-1, -1,  1; %C
 1, -1, -1; %D
 1,  1,  1; %A
-1,  1, -1; %B
];

scatter3(WX(idx,1),WX(idx,2),WX(idx,3),1,rgb_colours(idx,:),'filled'); 
hold on; 
plot3(WY(:,1),WY(:,2),WY(:,3),'k-','LineWidth',linesize); 
hold off;
%cabd_view_params(gcf,fontsize,1);
%figure; scatter3(WX(idx,1),WX(idx,2),WX(idx,3),1,rgb_colours(idx,:),'filled'); 
%cadb_view_params(gcf,fontsize,1);
%figure; scatter3(WX(idx,1),WX(idx,2),WX(idx,3),1,rgb_colours(idx,:),'filled'); 
%cabd_view_params(gcf,fontsize,1);
%figure; scatter3(WX(idx,1),WX(idx,2),WX(idx,3),1,rgb_colours(idx,:),'filled'); 
%acdb_view_params(gcf,fontsize,1);

% Koniec
