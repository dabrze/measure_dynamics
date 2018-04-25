function [] = acbd_gradient_view ( fig, fontsize, chars_visible, view_vect, letters )

% chars_visible:
%  0 -- none
%  1 -- letters
%  2 -- digits
%  3 -- letters+digits

if (exist('view_vect','var')==1),
	az = view_vect(1);
	el = view_vect(2);
else
	az = -70;
	el = +12;
end;

if ((chars_visible == 2)|(chars_visible == 3)),
	ticklabels = {'-1';'';'0';'';'+1'};
else
	ticklabels = {'';'';'';'';''};
	ticklabels = {};
end;

if (exist('letters','var')==1),
else
	letters = {'$TP$','$FP$','$FN$','$TN$'};
end;

%xlabel('x'); ylabel('y'); zlabel('z'); 
axis square; %colorbar; 
view(az,el);
set(gca,'XLim',[-1 +1]);
set(gca,'YLim',[-1 +1]);
set(gca,'ZLim',[-1 +1]);
set(gca,'XTick',[-1:0.5:+1]);
set(gca,'XTickLabel',ticklabels);
set(gca,'YTick',[-1:0.5:+1]);
set(gca,'YTickLabel',ticklabels);
set(gca,'ZTick',[-1:0.5:+1]);
set(gca,'ZTickLabel',ticklabels);

if ((chars_visible == 2)|(chars_visible == 3)),
else
%	set(gca,'XTick',[]);
%	set(gca,'XTickLabel',[]);
%	set(gca,'YTick',[]);
%	set(gca,'YTickLabel',[]);
%	set(gca,'ZTick',[]);
%	set(gca,'ZTickLabel',[]);
end;

if ((chars_visible == 1)|(chars_visible == 3)),
%	text( 1.00, 1.10,  1.20, letters{1});
%	text(-1.25, 1.10, -0.70, letters{2});
%	text(-1.00,-1.00,  1.20, letters{3});
%	text( 1.00,-1.10, -0.85, letters{4});
	text('string',letters{1},'interpreter','latex','pos',[ 1.00, 1.10, 1.20]);
	text('string',letters{2},'interpreter','latex','pos',[-1.25, 1.20,-0.70]);
	text('string',letters{3},'interpreter','latex','pos',[-1.00,-1.00, 1.20]);
	text('string',letters{4},'interpreter','latex','pos',[ 1.00,-1.10,-0.85]);
else
	% do nothing
end;

set(gca,'FontSize',fontsize);%,'fontWeight','bold');
set(findall(gcf,'type','text'),'FontSize',fontsize);%,'fontWeight','bold');

% Koniec
